package com.freedy.mall.order.web;

import com.freedy.mall.order.exception.NoStockException;
import com.freedy.mall.order.service.OrderService;
import com.freedy.mall.order.vo.OrderConfirmVo;
import com.freedy.mall.order.vo.OrderSubmitVo;
import com.freedy.mall.order.vo.SubmitOrderRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author Freedy
 * @date 2021/3/21 21:14
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo=orderService.confirmOrder();
        model.addAttribute("orderConfirmData",confirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, HttpSession session){
        System.out.println(vo);
        SubmitOrderRespVo respVo=new SubmitOrderRespVo();
        String msg="下单失败!";
        try {
            //创建订单 验证令牌 验价格 所库存
            respVo = orderService.submitOrder(vo);
        } catch (NoStockException e) {
            e.printStackTrace();
            respVo.setCode(3);
        }catch (Exception e){
            e.printStackTrace();
            respVo.setCode(4);
        }
        if (respVo.getCode()==0){
            model.addAttribute("OR",respVo);
            //下单成功来到支付页面
            return "pay";
        }else {
            switch (Objects.requireNonNull(respVo).getCode()){
                case 1:
                    msg+="订单信息过期,请刷新页面提交!";
                    break;
                case 2:
                    msg+="订单价格发生变化,请确认后再次提交!";
                    break;
                case 3:
                    msg+="商品库存不足!";
                case 4:
                    msg+="系统发生位置异常!";
            }
            session.setAttribute("orderMsg",msg);
            //下单失败回到订单确认页
            return "redirect:http://order.freedymall.com/toTrade";
        }
    }

}
