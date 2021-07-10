package com.freedy.mall.order;

import com.freedy.mall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate template;



    @Test
    public void sendMessage(){
        OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
        entity.setId(1L);
        entity.setName("哈哈");
        entity.setCreateTime(new Date());

        template.convertAndSend("hello-java-exchange",
                "hello.java",entity);
        log.info("消息发送完成{}",entity);
    }


    /**
     * 如何创建exchange queue binding
     */
    @Test
    public void contextLoads() {
        DirectExchange directExchange = new DirectExchange("hello-java-exchange",true,false);
        amqpAdmin.declareExchange(directExchange);
        log.info("exchange[{}]创建成功","hello-java-exchange");
    }

    @Test
    public void queue(){
        Queue queue = new Queue("hello-java-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        log.info("queue[{}]创建成功","hello-java-queue");
    }

    @Test
    public void bind(){
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,"hello-java-exchange",
                "hello.java",null
                );
        amqpAdmin.declareBinding(binding);
        log.info("binding[{}]创建成功","hello-java-queue");
    }

}
