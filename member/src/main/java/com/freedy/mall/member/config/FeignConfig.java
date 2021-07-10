package com.freedy.mall.member.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Freedy
 * @date 2021/3/22 8:53
 */
@Configuration
public class FeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            if (attributes!=null){
                String cookie = attributes.getRequest().getHeader("Cookie");
                //给新请求加入老请求的cookie
                requestTemplate.header("Cookie",cookie);
            }
        };
    }

}
