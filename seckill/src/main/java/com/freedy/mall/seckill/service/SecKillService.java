package com.freedy.mall.seckill.service;

import com.freedy.common.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/31 11:51
 */
public interface SecKillService {

    void uploadSecKillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    List<SeckillSkuRedisTo> getSkuSecKillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
