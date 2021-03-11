package com.freedy.mall.product.vo;

import com.freedy.mall.product.entity.SkuImagesEntity;
import com.freedy.mall.product.entity.SkuInfoEntity;
import com.freedy.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/6 23:01
 */
@Data
public class SkuItemVo {
    //sku基本信息的获取
    SkuInfoEntity info;
    Boolean hasStock=true;
    //sku的图片信息
    List<SkuImagesEntity> images;
    //获取spu的销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;
    //获取spu的介绍
    SpuInfoDescEntity desc;
    //获取spu的规格参数信息
    List<SpuItemAttrGroup> groupAttrs;
}
