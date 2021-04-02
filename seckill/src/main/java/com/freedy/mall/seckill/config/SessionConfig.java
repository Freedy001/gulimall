package com.freedy.mall.seckill.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @author Freedy
 * @date 2021/4/1 19:44
 */
@Configuration
public class SessionConfig {
    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        //设置session的访问域名
        serializer.setDomainName("freedymall.com");
        //设置setCookie的名字
        serializer.setCookieName("FREEDY-MALL-SESSION");
        return serializer;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(){
        //session序列化为json
        return new GenericFastJsonRedisSerializer();
    }
}