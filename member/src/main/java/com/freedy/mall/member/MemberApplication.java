package com.freedy.mall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1.想要远程调用别的服务
 * 2.引入open feign
 * 3.编写一个接口，告诉spring cloud 这个接口需要调用远程服务
 *      申明接口的每一个方法都是调用哪个远程服务的那个请求
 */
@EnableFeignClients(basePackages = "com.freedy.mall.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class MemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }

}
