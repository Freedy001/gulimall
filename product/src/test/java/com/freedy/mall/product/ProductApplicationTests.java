package com.freedy.mall.product;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductApplicationTests {

    @Autowired
    StringRedisTemplate template;

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void test(){
        ValueOperations<String, String> valOps = template.opsForValue();
        valOps.set("hello","world_"+ UUID.randomUUID());
        String hello = valOps.get("hello");
        System.out.println(hello);
    }

    @Test
    public void test1(){
        System.out.println(redissonClient);

    }
}
