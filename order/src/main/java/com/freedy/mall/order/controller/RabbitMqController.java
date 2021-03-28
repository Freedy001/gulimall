package com.freedy.mall.order.controller;


import com.freedy.common.utils.R;
import com.freedy.mall.order.entity.RefundInfoEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author Freedy
 * @date 2021/3/21 13:24
 */
@RestController
public class RabbitMqController {

    @Autowired
    RabbitTemplate template;

    @GetMapping("/sendMessage")
    public R sendMessage(){
        RefundInfoEntity entity = new RefundInfoEntity();
        entity.setId(12343L);
        entity.setRefundStatus(1);
        entity.setRefundSn("abcdefg I love your dddd");
        template.convertAndSend("hello-java-exchange","hello.java",entity);
        return R.ok();
    }

    @GetMapping("/sendErrorMessage")
    public R sendErrorMessage(boolean isExchange){
        RefundInfoEntity entity = new RefundInfoEntity();
        entity.setId(12343L);
        entity.setRefundStatus(1);
        entity.setRefundSn("abcdefg I love your dddd");
        if (isExchange){
            template.convertAndSend("hello-java-exchange","12lo.java",entity);
        }else {
            template.convertAndSend("hello-java-exch12e","hello.java",entity);
        }
        return R.ok();
    }
}
