package com.freedy.mall.seckill.controller;

import com.freedy.common.to.MemberEntity;
import com.freedy.common.to.SeckillSkuRedisTo;
import com.freedy.common.utils.R;
import com.freedy.mall.seckill.interceptor.LoginUserInterceptor;
import com.freedy.mall.seckill.service.SecKillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/31 19:57
 */
@Controller
public class SeckillController {

    @Autowired
    SecKillService secKillService;

    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> vos = secKillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    @ResponseBody
    @GetMapping("/seckill/{skuId}")
    public R getSkuSecKillInfo(@PathVariable Long skuId) {
        return R.ok().setData(secKillService.getSkuSecKillInfo(skuId));
    }

    @GetMapping("/kill")
    public String killSku(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) {
        String orderSn=secKillService.kill(killId,key,num);
        model.addAttribute("orderSn",orderSn);
        return "success";
    }


}
