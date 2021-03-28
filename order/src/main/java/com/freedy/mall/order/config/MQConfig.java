package com.freedy.mall.order.config;

import com.freedy.mall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Freedy
 * @date 2021/3/28 16:59
 */
@Configuration
public class MQConfig {
    /**
     * 容器中的Queue Exchange Binding都会自动创建
     */
    @Bean
    public Queue OrderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        //死信交换机
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        //死信路由键
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        // 消息过期时间 1分钟
        arguments.put("x-message-ttl", 60000);
        return new Queue("order.delay.queue",
                true, false, false, arguments);
    }

    @Bean
    public Queue OrderReleaseQueue() {
        return new Queue("order.release.queue",
                true, false, false);
    }

    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange",
                true, false);
    }

    @Bean
    public Binding orderCreateOrderBinding() {
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order", null);
    }

    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding("order.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order", null);
    }


}
