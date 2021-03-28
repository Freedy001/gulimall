package com.freedy.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.freedy.common.to.MemberEntity;
import com.freedy.common.utils.R;
import com.freedy.mall.order.constant.OrderConstant;
import com.freedy.mall.order.entity.OrderItemEntity;
import com.freedy.mall.order.enume.OrderStatusEnum;
import com.freedy.mall.order.exception.NoStockException;
import com.freedy.mall.order.feign.CartFeignService;
import com.freedy.mall.order.feign.MemberFeignService;
import com.freedy.mall.order.feign.ProductFeignService;
import com.freedy.mall.order.feign.WmsFeignService;
import com.freedy.mall.order.interceptor.LoginUserInterceptor;
import com.freedy.mall.order.service.OrderItemService;
import com.freedy.mall.order.to.OrderCreateTo;
import com.freedy.mall.order.vo.*;
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
        String script="if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script,Long.class),
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
            if (payAmount.subtract(payPrice).abs().doubleValue()<0.01){
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
                if (r.getCode()==0){
                    //锁定成功
                    respVo.setOrder(order.getOrder());
                    respVo.setCode(0);
                    int i=10/0;
                    return respVo;
                }else {
                    //锁定失败
                    throw new NoStockException();
                }
            }else {
                respVo.setCode(2);
                return respVo;
            }
        }
    }

    /**
     * 保存订单数据
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> item = order.getOrderItem();
        orderItemService.saveBatch(item);
    }


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
        Integer giftIntegration=0;
        Integer giftGrowth=0;

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
        orderEntity.setConfirmStatus(7);
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
        itemEntity.setGiftGrowth(itemVo.getPrice().intValue()*itemEntity.getSkuQuantity());
        itemEntity.setGiftIntegration(itemVo.getPrice().intValue()*itemEntity.getSkuQuantity());
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

}