package com.freedy.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Freedy
 * @date 2021/3/8 20:23
 */
@Data
@ConfigurationProperties("freedymall.thread")
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
