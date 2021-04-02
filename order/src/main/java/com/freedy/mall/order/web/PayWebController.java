package com.freedy.mall.order.web;

import com.alipay.api.AlipayApiException;
import com.freedy.mall.order.config.AlipayTemplate;
import com.freedy.mall.order.service.OrderService;
import com.freedy.mall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Freedy
 * @date 2021/3/30 16:14
 */
@Controller
public class PayWebController {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo =  orderService.getOrderPay(orderSn);
        return alipayTemplate.pay(payVo);
    }

}
