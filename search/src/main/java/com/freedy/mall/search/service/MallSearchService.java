package com.freedy.mall.search.service;

import com.freedy.mall.search.vo.SearchParam;
import com.freedy.mall.search.vo.SearchResult;

/**
 * @author Freedy
 * @date 2021/3/2 19:16
 */
public interface MallSearchService {
    /**
     * 商品的检索功能
     * @param param 检索的所有参数
     * @return 里面包含页面所需的全部信息
     */
    SearchResult search(SearchParam param);
}
