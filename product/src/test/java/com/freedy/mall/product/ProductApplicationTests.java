package com.freedy.mall.product;

import com.alibaba.fastjson.JSON;
import com.freedy.common.utils.JsonBeautify;
import com.freedy.mall.product.dao.AttrGroupDao;
import com.freedy.mall.product.dao.SkuSaleAttrValueDao;
import com.freedy.mall.product.vo.SkuItemSaleAttrVo;
import com.freedy.mall.product.vo.SkuItemVo;
import com.freedy.mall.product.vo.SpuItemAttrGroup;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductApplicationTests {

    @Autowired
    StringRedisTemplate template;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;



    @Test
    public void test(){
        ValueOperations<String, String> valOps = template.opsForValue();
        valOps.set("hello","world_"+ UUID.randomUUID());
        String hello = valOps.get("hello");
        System.out.println(hello);
    }

    @Test
    public void test1(){
        List<SkuItemSaleAttrVo> vos = skuSaleAttrValueDao.getSaleAttrsBySpuId(7L);
        System.out.println(JsonBeautify.JsonFormat(JSON.toJSONString(vos)));
    }
}
