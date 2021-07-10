package com.freedy.mall.product.web;

import com.freedy.mall.product.service.SkuInfoService;
import com.freedy.mall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * @author Freedy
 * @date 2021/3/6 15:30
 */
@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable Long skuId, Model model) throws ExecutionException, InterruptedException {
        System.out.println("准备查询skuid:"+skuId+"的详情");
        SkuItemVo itemVo= skuInfoService.item(skuId);
        model.addAttribute("item",itemVo);
        return "item";
    }
}
