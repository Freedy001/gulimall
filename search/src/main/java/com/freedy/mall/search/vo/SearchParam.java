package com.freedy.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/2 19:14
 *
 * 封装所有可能传递过来的查询条件
 * catalog3Id=225&keyword=你好&sort=saleCount_asc&hasStock=0/1&brandId=2
 */
@Data
public class SearchParam {
    private String keyword;
    private Long catalog3Id;
    /**
     * 排序条件
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;
    /**
     * 过滤条件
     * hasStock、skuPrice、brandId、attr
     * hasStock=0/1是否只显示有货
     * skuPrice=1_500/_500/500_价格区间
     * brandId=2&brandId=253&brandId=253 品牌选择(可多选)
     * attrs=1_其他:安卓&attrs=2_5寸:6寸  '_'前面代表属性编号，后面代表属性使用':'分割
     * pageNum页码
     */
    private Integer hasStock=1;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    private Integer pageNum=1;

    private String queryString;
}
