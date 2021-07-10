package com.freedy.mall.order.listenner;

import com.alipay.api.internal.util.AlipaySignature;
import com.freedy.mall.order.config.AlipayTemplate;
import com.freedy.mall.order.service.OrderService;
import com.freedy.mall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Freedy
 * @date 2021/3/30 19:27
 */
@RestController
public class OrderPayedListener {

    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;

    @PostMapping("/payed/notify")
    public String handleAliPayed(PayAsyncVo vo,HttpServletRequest request) throws Exception {
        //验签
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            //valueStr = new String(valueStr.getBytes(StandardCharsets.ISO_8859_1),
            //        StandardCharsets.UTF_8);
            params.put(name, valueStr);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                alipayTemplate.alipay_public_key,
                alipayTemplate.charset,
                alipayTemplate.sign_type); //调用SDK验证签名
        if (signVerified){
            System.out.println("签名验证成功");
            //只要我们收到支付宝给我们异步的结果通知，告诉我们交易成功。
            // 返回success，支付宝就再也不通知
            return orderService.handlePayResult(vo);
        }else {
            System.out.println("签名验证失败");
            return "error";
        }
    }

}
