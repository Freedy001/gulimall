package com.freedy.mall.search.controller;

import com.freedy.mall.search.service.MallSearchService;
import com.freedy.mall.search.vo.SearchParam;
import com.freedy.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Freedy
 * @date 2021/3/2 18:44
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    /**
     * 自动将页面提交过来的请求参数封装成指定对象
     * @param param
     * @param model
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model) {
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);
        return "list";
    }
}
