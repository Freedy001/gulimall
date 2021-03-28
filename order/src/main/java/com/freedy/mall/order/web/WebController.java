package com.freedy.mall.order.web;

import com.freedy.common.utils.R;
import com.freedy.mall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @author Freedy
 * @date 2021/3/21 16:49
 */
@Controller
public class WebController {

    @GetMapping("/confirm.html")
    public String confirm(){
        return "confirm";
    }
    @GetMapping("/detail.html")
    public String detail(){
        return "detail";
    }
    @GetMapping("/list.html")
    public String list(){
        return "list";
    }
    @GetMapping("/pay.html")
    public String pay(){
        return "pay";
    }

    @Autowired
    RabbitTemplate template;

    @ResponseBody
    @GetMapping("/test/createOrder")
    public R createOrder(){
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(UUID.randomUUID().toString());
        entity.setModifyTime(new Date());
        template.convertAndSend("order-event-exchange",
                "order.create.order",entity);
        return R.ok();
    }

}
