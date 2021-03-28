package com.freedy.mall.order.config;

import com.rabbitmq.client.impl.AMQImpl;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Freedy
 * @date 2021/3/20 21:19
 */
@Configuration
public class RabbitConfig {

    @Autowired
    RabbitTemplate template;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @PostConstruct//RabbitConfig对象创建完成后,执行这个方法
    public void initRabbitTemplate() {
        template.setConfirmCallback((data, ack, err) -> {
            System.out.println("confirm");
            System.out.println(data);
            System.out.println(ack);
            System.out.println(err);
        });
        template.setReturnCallback((Message message, int replyCode, String replyText,
                                    String exchange, String routingKey) -> {
            System.out.println(message);
            System.out.println(replyCode);
            System.out.println(replyText);
            System.out.println(exchange);
            System.out.println(routingKey);
        });
    }

}
