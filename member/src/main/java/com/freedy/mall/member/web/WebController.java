package com.freedy.mall.member.web;

import com.alibaba.fastjson.JSON;
import com.freedy.common.utils.JsonBeautify;
import com.freedy.common.utils.R;
import com.freedy.mall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

/**
 * @author Freedy
 * @date 2021/3/30 17:12
 */
@Controller
public class WebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String list(@RequestParam(value = "pageNum" ,defaultValue = "1")
                                   Integer pageNum, Model model){
        HashMap<String, Object> map = new HashMap<>();
        map.put("page",pageNum.toString());
        R r = orderFeignService.listWithItem(map);
        model.addAttribute("order",r);
        System.out.println(JsonBeautify.JsonFormat(JSON.toJSONString(r)));
        return "list";
    }

}
