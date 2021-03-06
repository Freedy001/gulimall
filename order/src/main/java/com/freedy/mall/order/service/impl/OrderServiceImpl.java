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
        //?????????????????????RequestAttributes?????????????????????????????????
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            //?????????????????????????????????
            List<MemberAddressVo> address = memberFeignService.getAddress(entity.getId());
            confirmVo.setAddress(address);
        }, poolExecutor);
        CompletableFuture<Void> f2 = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            //?????????????????????????????????????????????
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
            //??????????????????
            Integer integration = entity.getIntegration();
            confirmVo.setIntegration(integration);
        }, poolExecutor);
        // TODO: 2021/3/21 ????????????
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
            //??????????????????
            respVo.setCode(1);
            return respVo;
        } else {
            //??????????????????
            //????????????
            OrderCreateTo order = createOrder();
            //??????
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (payAmount.subtract(payPrice).abs().doubleValue() < 0.01) {
                //????????????
                //????????????
                saveOrder(order);
                //????????????.????????????????????????
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
                    //????????????
                    respVo.setOrder(order.getOrder());
                    respVo.setCode(0);
                    // TODO: 2021/3/29 ?????????????????????????????????mq
                    rabbitTemplate.convertAndSend(
                            "order-event-exchange",
                            "order.create.order",
                            order.getOrder());
                    return respVo;
                } else {
                    //????????????
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
        //?????????????????????????????????
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())){
            System.out.println("???????????????????????????????????????????????????"+entity.getOrderSn());
            baseMapper.updateStatus(orderEntity.getId(),OrderStatusEnum.CANCLED.getCode());
            OrderTo orderTo = new OrderTo(orderEntity.getOrderSn());
            // TODO: 2021/3/29 ???????????????????????????????????????????????????????????????????????????
            // TODO: 2021/3/29 ??????????????????????????????????????????????????????
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
        payVo.setSubject("freedymall??????:"+entity.getSkuName()+"???");
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
            //??????????????????
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
        //???????????????
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(quickOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(allPrice);
        orderItemEntity.setSkuQuantity(quickOrderTo.getNum());
        Long skuId = quickOrderTo.getSkuId();
        orderItemEntity.setSkuId(skuId);
        //?????????spu??????
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
     * ????????????
     */
    private OrderCreateTo createOrder() {
        MemberEntity memberInfo = LoginUserInterceptor.loginUser.get();
        OrderSubmitVo vo = confirmVoThreadLocal.get();
        OrderCreateTo createTo = new OrderCreateTo();
        //?????????????????????
        String timeId = IdWorker.getTimeId();
        //????????????
        OrderEntity orderEntity = new OrderEntity();
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> {
            MemberReceiveAddressVo address = memberFeignService.getSingleAddress(vo.getAddrId());
            //??????????????????????????????
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
        //???????????????
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
        //????????????????????????????????????
        BigDecimal sum = new BigDecimal("0");
        BigDecimal couponAmount = new BigDecimal("0");
        BigDecimal integrationAmount = new BigDecimal("0");
        BigDecimal promotionAmount = new BigDecimal("0");
        Integer giftIntegration = 0;
        Integer giftGrowth = 0;

        for (OrderItemEntity entity : createTo.getOrderItem()) {
            //????????????
            sum = sum.add(entity.getRealAmount());
            couponAmount = couponAmount.add(entity.getCouponAmount());
            integrationAmount = couponAmount.add(entity.getIntegrationAmount());
            promotionAmount = couponAmount.add(entity.getPromotionAmount());
            //??????
            giftIntegration += entity.getGiftIntegration();
            giftGrowth += entity.getGiftGrowth();
        }
        orderEntity.setTotalAmount(sum);
        orderEntity.setPayAmount(sum.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setPromotionAmount(promotionAmount);
        //??????????????????
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setConfirmStatus(0);
        //????????????????????????
        orderEntity.setIntegration(giftIntegration);
        orderEntity.setGrowth(giftGrowth);
        orderEntity.setDeleteStatus(0);//?????????
        return createTo;
    }

    /**
     * ???????????????
     */
    private OrderItemEntity buildOrderItem(OrderItemVo itemVo, String id) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(id);
        //?????????sku??????
        itemEntity.setSkuId(itemVo.getSkuId());
        itemEntity.setSkuName(itemVo.getTitle());
        itemEntity.setSkuPic(itemVo.getImage());
        itemEntity.setSkuPrice(itemVo.getPrice());
        itemEntity.setSkuQuantity(itemVo.getCount());
        String[] attr = itemVo.getSkuAttr().toArray(new String[0]);
        String joinAttr = String.join(";", attr);
        itemEntity.setSkuAttrsVals(joinAttr);
        //?????????spu??????
        Long skuId = itemVo.getSkuId();
        R res = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = res.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());
        //????????????
        itemEntity.setGiftGrowth(itemVo.getPrice().intValue() * itemEntity.getSkuQuantity());
        itemEntity.setGiftIntegration(itemVo.getPrice().intValue() * itemEntity.getSkuQuantity());
        //todo ????????????
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        //??????????????????????????????
        itemEntity.setRealAmount(itemEntity.getSkuPrice().multiply(
                new BigDecimal(itemEntity.getSkuQuantity().toString())
        )
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount()));
        return itemEntity;
    }

    /**
     * ??????????????????
     */
    private void saveOrder(OrderCreateTo order) {
        this.save(order.getOrder());
        orderItemService.saveBatch(order.getOrderItem());
    }

}