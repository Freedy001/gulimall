package com.freedy.mall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/7 13:42
 */
@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValuesWithSkuId> attrValues;
}
