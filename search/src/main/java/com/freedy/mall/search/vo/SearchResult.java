package com.freedy.mall.search.vo;

import com.freedy.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/2 20:32
 * 返回给页面的全部信息
 */
@Data
public class SearchResult {
    private List<SkuEsModel> product;

    private Integer pageNum;//当前页码
    private Long total;//总记录数
    private Integer totalPage;//总页码

    private List<BrandVo> brands;//当前查询到的结果,所有涉及到的品牌
    private List<CatalogVo> catalogs;
    private List<AttrVo> attrs;

    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
        private List<String> catelogValue;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private String attrValue;
    }


}
