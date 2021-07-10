package com.freedy.mall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.freedy.common.to.MemberEntity;
import com.freedy.common.to.SeckillSkuRedisTo;
import com.freedy.common.to.mq.QuickOrderTo;
import com.freedy.common.utils.R;
import com.freedy.mall.seckill.feign.CouponFeignService;
import com.freedy.mall.seckill.feign.ProductFeignService;
import com.freedy.mall.seckill.interceptor.LoginUserInterceptor;
import com.freedy.mall.seckill.service.SecKillService;
import com.freedy.mall.seckill.vo.SeckillSessionEntity;
import com.freedy.mall.seckill.vo.SeckillSkuRelationEntity;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Freedy
 * @date 2021/3/31 11:52
 */
@Service
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions";

    private final String SKU_KILL_CACHE_PREFIX = "seckill:skus";

    private final String SKU_Stock_SEMAPHORE = "seckill:stock:";//+商品随机码


    @Override
    public void uploadSecKillSkuLatest3Days() {
        //去扫描最近三天需要参与秒杀的活动
        R session = couponFeignService.getLatest3DaySession();
        List<SeckillSessionEntity> data = session.getData(new TypeReference<List<SeckillSessionEntity>>() {
        });
        //缓存到reds
        //缓存活动信息
        //幂等性操作 清理场次
        Set<String> keys1 = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        if (keys1 != null) {
            redisTemplate.delete(keys1);
        }
        saveSessionInfos(data);
        //清理过期的商品
        clearExpiredProduct();
        //缓存活动的关联的商品消息
        saveSessionSkuInfos(data);
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //确定当前时间属于哪个秒杀场次
        long time = new Date().getTime();
        for (String key : Objects.requireNonNull(redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*"))) {
            String timeString = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] times = timeString.split("_");
            long start = Long.parseLong(times[0]);
            long end = Long.parseLong(times[1]);
            if (time >= start && time <= end) {
                //获取当前场次的信息
                List<String> range = redisTemplate.opsForList().range(key, Long.MIN_VALUE, Long.MAX_VALUE);
                BoundHashOperations<String, String, Object> ops = redisTemplate.boundHashOps(SKU_KILL_CACHE_PREFIX);
                List<Object> list = ops.multiGet(range);
                if (list != null) {
                    return list.stream().map(
                            item -> JSON.parseObject((String) item,
                                    SeckillSkuRedisTo.class))
                            .collect(Collectors.toList());
                }
                break;
            }
        }
        return null;
    }

    @Override
    public List<SeckillSkuRedisTo> getSkuSecKillInfo(Long skuId) {
        //找到所有需要参与秒杀的key
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKU_KILL_CACHE_PREFIX);
        Set<String> keys = ops.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            List<SeckillSkuRedisTo> redisTos = new ArrayList<>();
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String o = ops.get(key);
                    SeckillSkuRedisTo redisTo = JSON.parseObject(o, SeckillSkuRedisTo.class);
                    long current = new Date().getTime();
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    if (startTime > current || current > endTime) {
                        redisTo.setRandomCode(null);
                    }
                    redisTos.add(redisTo);
                }
            }
            return redisTos;
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKU_KILL_CACHE_PREFIX);
        String sku = ops.get(killId);
        if (StringUtils.hasText(sku)) {
            SeckillSkuRedisTo redisTo = JSON.parseObject(sku, SeckillSkuRedisTo.class);
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            long current = new Date().getTime();
            //校验时间
            if (current >= startTime && current <= endTime) {
                //校验随机码
                if (redisTo.getRandomCode().equals(key)) {
                    //验证购物数量
                    if (num <= redisTo.getSeckillLimit().intValue()) {
                        //验证这个人是否购买过 每次秒杀成功就去占位。
                        //根据userId_sessionId_skuId来判断是否购买过
                        MemberEntity entity = LoginUserInterceptor.loginUser.get();
                        String userKey = entity.getId() + "_" +
                                redisTo.getPromotionSessionId() + "_"+
                                redisTo.getSkuId();
                        //自动过期
                        Boolean ifAbsent = redisTemplate.opsForValue()
                                .setIfAbsent(userKey, num.toString(),
                                endTime - startTime, TimeUnit.MILLISECONDS);
                        if (ifAbsent){
                            //占位成功
                            RSemaphore semaphore = redisson.getSemaphore(SKU_Stock_SEMAPHORE + key);
                            try {
                                Boolean acquire = semaphore.tryAcquire(num);
                                if (acquire){
                                    //秒杀成功
                                    //快速下单 发送MQ消息
                                    String orderSn = IdWorker.getTimeId();
                                    QuickOrderTo to = new QuickOrderTo();
                                    to.setOrderSn(orderSn);
                                    to.setNum(num);
                                    to.setMemberId(entity.getId());
                                    to.setPromotionSessionId(redisTo.getPromotionSessionId());
                                    to.setSkuId(redisTo.getSkuId());
                                    to.setSeckillPrice(redisTo.getSeckillPrice());
                                    rabbitTemplate.convertAndSend("order-event-exchange",
                                            "order.seckill.order",
                                            to);
                                    return orderSn;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void saveSessionInfos(List<SeckillSessionEntity> data) {
        for (SeckillSessionEntity datum : data) {
            if (datum.getStatus() == 1) {
                long startTime = datum.getStartTime().getTime();
                long endTime = datum.getEndTime().getTime();
                String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
                List<String> ids = datum.getRelationSkus().stream()
                        .map(item -> datum.getId().toString() + "_" + item.getSkuId().toString())
                        .collect(Collectors.toList());
                //缓存活动消息
                redisTemplate.opsForList().leftPushAll(key, ids);
            }
        }
    }

    private void saveSessionSkuInfos(List<SeckillSessionEntity> data) {
        for (SeckillSessionEntity datum : data) {
            if (datum.getStatus() == 1) {
                //准备hash操作
                BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKU_KILL_CACHE_PREFIX);
                for (SeckillSkuRelationEntity skus : datum.getRelationSkus()) {
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                    //sku的基本信息
                    R bySkuId = productFeignService.info(skus.getSkuId());
                    SeckillSkuRedisTo skuInfos = bySkuId.getData("skuInfo", new TypeReference<SeckillSkuRedisTo>() {
                    });
                    BeanUtils.copyProperties(skuInfos, seckillSkuRedisTo);
                    //sku秒杀信息
                    BeanUtils.copyProperties(skus, seckillSkuRedisTo);
                    //设置上当前商品的秒杀时间信息
                    seckillSkuRedisTo.setStartTime(datum.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(datum.getEndTime().getTime());
                    //商品的随机码
                    String token = UUID.randomUUID().toString().replace("-", "");
                    seckillSkuRedisTo.setRandomCode(token);
                    //引入分布式信号量  限流
                    RSemaphore semaphore = redisson.getSemaphore(SKU_Stock_SEMAPHORE + token);
                    //商品可以秒杀的数量作为信号量
                    String skuInfoKey = skus.getPromotionSessionId().toString()
                            + "_" + skus.getSkuId().toString();
                    Object key = ops.get(skuInfoKey);
                    //幂等性操作 防止多次设置导致token发生变化
                    if (key == null) {
                        semaphore.trySetPermits(skus.getSeckillCount().intValue());
                    } else {
                        SeckillSkuRedisTo redisTo = JSON.parseObject((String) key, SeckillSkuRedisTo.class);
                        seckillSkuRedisTo.setRandomCode(redisTo.getRandomCode());
                    }
                    ops.put(skuInfoKey, JSON.toJSONString(seckillSkuRedisTo));
                }
            }
        }
    }

    private void clearExpiredProduct() {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKU_KILL_CACHE_PREFIX);
        Set<String> keys = ops.keys();
        if (keys != null) {
            for (String key : keys) {
                String sku = ops.get(key);
                SeckillSkuRedisTo redisTo = JSON.parseObject(sku, SeckillSkuRedisTo.class);
                Long endTime = redisTo.getEndTime();
                if (endTime < new Date().getTime()) {
                    String token = redisTo.getRandomCode();
                    redisTemplate.delete(SKU_Stock_SEMAPHORE + token);
                    ops.delete(key);
                }
            }
        }
    }

}
