package com.freedy.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.freedy.common.to.MemberEntity;
import com.freedy.common.to.mq.OrderTo;
import com.freedy.common.to.mq.QuickOrderTo;
import com.freedy.common.to.mq.StockLockedTo;
import com.freedy.common.utils.R;
import com.freedy.mall.order.constant.OrderConstant;
import com.freedy.mall.order.entity.OrderItemEntity;
import com.freedy.mall.order.entity.PaymentInfoEntity;
import com.freedy.mall.order.enume.OrderStatusEnum;
import com.freedy.mall.order.exception.NoStockException;
import com.freedy.mall.order.feign.CartFeignService;
import com.freedy.mall.order.feign.MemberFeignService;
import com.freedy.mall.order.feign.ProductFeignService;
import com.freedy.mall.order.feign.WmsFeignService;
import com.freedy.mall.order.interceptor.LoginUserInterceptor;
import com.freedy.mall.order.service.OrderItemService;
import com.freedy.mall.order.service.PaymentInfoService;
import com.freedy.mall.order.to.OrderCreateTo;
import com.freedy.mall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.Query;

import com.freedy.mall.order.dao.OrderDao;
import com.freedy.mall.order.entity.OrderEntity;
import com.freedy.mall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();
    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private ThreadPoolExecutor poolExecutor;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberEntity entity = LoginUserInterceptor.loginUser.get();
        //获取当前线程的RequestAttributes然后传给异步调用的线程
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            //远程查询所有的收货地址
            List<MemberAddressVo> address = memberFeignService.getAddress(entity.getId());
            confirmVo.setAddress(address);
        }, poolExecutor);
        CompletableFuture<Void> f2 = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            //远程查出购物车所有选中的购物项
            List<OrderItemVo> item = cartFeignService.getItem();
            confirmVo.setItems(item);
        }, poolExecutor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            List<SkuHasStockVo> stockData = wmsFeignService.getSkusHasStock(skuIds).getData(new TypeReference<List<SkuHasStockVo>>() {
            });
            for (OrderItemVo item : items) {
                for (SkuHasStockVo stockDatum : stockData) {
                    if (item.getSkuId().equals(stockDatum.getSkuID())) {
                        item.setHasStock(stockDatum.isHasStock());
                    }
                }
            }
            confirmVo.setItems(items);
        });
        CompletableFuture<Void> f3 = CompletableFuture.runAsync(() -> {
            //查询用户积分
            Integer integration = entity.getIntegration();
            confirmVo.setIntegration(integration);
        }, poolExecutor);
        // TODO: 2021/3/21 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(token);
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + entity.getId(), token, 30, TimeUnit.MINUTES);
        CompletableFuture.allOf(f1, f2, f3).get();
        return confirmVo;
    }

    @Transactional
    @Override
    public SubmitOrderRespVo submitOrder(OrderSubmitVo vo) {
        confirmVoThreadLocal.set(vo);
        MemberEntity entity = LoginUserInterceptor.loginUser.get();
        SubmitOrderRespVo respVo = new SubmitOrderRespVo();
        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Collections.singletonList(OrderConstant.USER_ORDER_TOKEN_PREFIX + entity.getId()),
                vo.getOrderToken());
        if (execute == null || execute == 0L) {
            //令牌验证失败
            respVo.setCode(1);
            return respVo;
        } else {
            //令牌验证成功
            //创建订单
            OrderCreateTo order = createOrder();
            //验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (payAmount.subtract(payPrice).abs().doubleValue() < 0.01) {
                //金额对比
                //保存订单
                saveOrder(order);
                //锁定库存.只要有异常就回滚
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> itemVos = order.getOrderItem().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(itemVos);
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    //锁定成功
                    respVo.setOrder(order.getOrder());
                    respVo.setCode(0);
                    // TODO: 2021/3/29 订单创建成功发送消息给mq
                    rabbitTemplate.convertAndSend(
                            "order-event-exchange",
                            "order.create.order",
                            order.getOrder());
                    return respVo;
                } else {
                    //锁定失败
                    throw new NoStockException();
                }
            } else {
                respVo.setCode(2);
                return respVo;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //查询当前订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())){
            System.out.println("收到过期的订单信息，准备关闭订单："+entity.getOrderSn());
            baseMapper.updateStatus(orderEntity.getId(),OrderStatusEnum.CANCLED.getCode());
            OrderTo orderTo = new OrderTo(orderEntity.getOrderSn());
            // TODO: 2021/3/29 保证消息一定会发出去，每一个消息都可以做好日志记录
            // TODO: 2021/3/29 定期扫描数据库将失败的消息在发送一遍
            rabbitTemplate.convertAndSend(
                    "order-event-exchange",
                    "order.release.other",
                    orderTo);
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity byOrderSn = getOrderByOrderSn(orderSn);
        payVo.setOut_trade_no(orderSn);
        payVo.setTotal_amount(String.valueOf(byOrderSn.getPayAmount().setScale(2,BigDecimal.ROUND_UP)));
        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity entity = order_sn.get(0);
        payVo.setSubject("freedymall收银:"+entity.getSkuName()+"等");
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
                        .eq("member_id",memberEntity.getId())
                        .orderByDesc("id")
        );
        page.getRecords().forEach(item-> {
            List<OrderItemEntity> list = orderItemService.list(
                    new QueryWrapper<OrderItemEntity>()
                            .eq("order_sn", item.getOrderSn())
            );
            item.setOrderItems(list);
        });
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        OrderEntity orderSn = getOrderByOrderSn(vo.getOut_trade_no());
        try {
            //保存交易流水
            PaymentInfoEntity infoEntity = new PaymentInfoEntity();
            infoEntity.setAlipayTradeNo(vo.getTrade_no());
            infoEntity.setOrderSn(vo.getOut_trade_no());
            infoEntity.setPaymentStatus(vo.getTrade_status());
            infoEntity.setCallbackTime(vo.getNotify_time());
            paymentInfoService.save(infoEntity);
            if (vo.getTrade_status().equals("TRADE_SUCCESS")||
                    vo.getTrade_status().equals("TRADE_FINISHED")){
                baseMapper.updateStatusByOrderSn(vo.getOut_trade_no(),OrderStatusEnum.PAYED.getCode());
            }
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @Override
    public void createSecKillOrder(QuickOrderTo quickOrderTo) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(quickOrderTo.getOrderSn());
        orderEntity.setMemberId(quickOrderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal allPrice = quickOrderTo.getSeckillPrice().multiply(new BigDecimal("" + quickOrderTo.getNum()));
        orderEntity.setPayAmount(allPrice);
        this.save(orderEntity);
        //设置订单项
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(quickOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(allPrice);
        orderItemEntity.setSkuQuantity(quickOrderTo.getNum());
        Long skuId = quickOrderTo.getSkuId();
        orderItemEntity.setSkuId(skuId);
        //商品的spu信息
        R res = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = res.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setCategoryId(data.getCatalogId());
        orderItemService.save(orderItemEntity);
    }

    /**
     * 创建订单
     */
    private OrderCreateTo createOrder() {
        MemberEntity memberInfo = LoginUserInterceptor.loginUser.get();
        OrderSubmitVo vo = confirmVoThreadLocal.get();
        OrderCreateTo createTo = new OrderCreateTo();
        //生成一个订单号
        String timeId = IdWorker.getTimeId();
        //设置订单
        OrderEntity orderEntity = new OrderEntity();
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> {
            MemberReceiveAddressVo address = memberFeignService.getSingleAddress(vo.getAddrId());
            //设置收货人地址等信息
            orderEntity.setMemberId(memberInfo.getId());
            orderEntity.setOrderSn(timeId);
            orderEntity.setReceiverName(address.getName());
            orderEntity.setReceiverCity(address.getCity());
            orderEntity.setReceiverRegion(address.getRegion());
            orderEntity.setReceiverPhone(address.getPhone());
            orderEntity.setReceiverDetailAddress(address.getDetailAddress());
            orderEntity.setReceiverProvince(address.getProvince());
            orderEntity.setReceiverPostCode(address.getPostCode());
            orderEntity.setFreightAmount(new BigDecimal(vo.getAddrId() * 10 + ""));
            orderEntity.setModifyTime(new Date());
            createTo.setOrder(orderEntity);
        }, poolExecutor);
        //构建订单项
        RequestAttributes reqAttr = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> f2 = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(reqAttr);
            List<OrderItemVo> item = cartFeignService.getItem();
            if (item != null && item.size() > 0) {
                List<OrderItemEntity> itemList = item.stream().
                        map(i -> buildOrderItem(i, timeId)).collect(Collectors.toList());
                createTo.setOrderItem(itemList);
            }
        }, poolExecutor);
        try {
            f1.get();
            f2.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //计算价格、积分等相关信息
        BigDecimal sum = new BigDecimal("0");
        BigDecimal couponAmount = new BigDecimal("0");
        BigDecimal integrationAmount = new BigDecimal("0");
        BigDecimal promotionAmount = new BigDecimal("0");
        Integer giftIntegration = 0;
        Integer giftGrowth = 0;

        for (OrderItemEntity entity : createTo.getOrderItem()) {
            //获取价格
            sum = sum.add(entity.getRealAmount());
            couponAmount = couponAmount.add(entity.getCouponAmount());
            integrationAmount = couponAmount.add(entity.getIntegrationAmount());
            promotionAmount = couponAmount.add(entity.getPromotionAmount());
            //积分
            giftIntegration += entity.getGiftIntegration();
            giftGrowth += entity.getGiftGrowth();
        }
        orderEntity.setTotalAmount(sum);
        orderEntity.setPayAmount(sum.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setPromotionAmount(promotionAmount);
        //设置订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setConfirmStatus(0);
        //设置订单积分信息
        orderEntity.setIntegration(giftIntegration);
        orderEntity.setGrowth(giftGrowth);
        orderEntity.setDeleteStatus(0);//未删除
        return createTo;
    }

    /**
     * 构建订单项
     */
    private OrderItemEntity buildOrderItem(OrderItemVo itemVo, String id) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(id);
        //商品的sku信息
        itemEntity.setSkuId(itemVo.getSkuId());
        itemEntity.setSkuName(itemVo.getTitle());
        itemEntity.setSkuPic(itemVo.getImage());
        itemEntity.setSkuPrice(itemVo.getPrice());
        itemEntity.setSkuQuantity(itemVo.getCount());
        String[] attr = itemVo.getSkuAttr().toArray(new String[0]);
        String joinAttr = String.join(";", attr);
        itemEntity.setSkuAttrsVals(joinAttr);
        //商品的spu信息
        Long skuId = itemVo.getSkuId();
        R res = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = res.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());
        //积分信息
        itemEntity.setGiftGrowth(itemVo.getPrice().intValue() * itemEntity.getSkuQuantity());
        itemEntity.setGiftIntegration(itemVo.getPrice().intValue() * itemEntity.getSkuQuantity());
        //todo 优惠信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        //当前订单项的实际金额
        itemEntity.setRealAmount(itemEntity.getSkuPrice().multiply(
                new BigDecimal(itemEntity.getSkuQuantity().toString())
        )
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount()));
        return itemEntity;
    }

    /**
     * 保存订单数据
     */
    private void saveOrder(OrderCreateTo order) {
        this.save(order.getOrder());
        orderItemService.saveBatch(order.getOrderItem());
    }

}