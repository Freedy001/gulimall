package com.freedy.mall.product.web;

import com.freedy.mall.product.entity.CategoryEntity;
import com.freedy.mall.product.service.CategoryService;
import com.freedy.mall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author Freedy
 * @date 2021/2/22 23:56
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/index.html", "/"})
    public String indexPage(Model model) {
        List<CategoryEntity> entities = categoryService.getLevel1Categories();
        model.addAttribute("categories", entities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson(){
        return categoryService.getCatelogJson();
    }

    @ResponseBody
    @GetMapping("/product/hello")
    public String hello(){
        return "hello";
    }


}
