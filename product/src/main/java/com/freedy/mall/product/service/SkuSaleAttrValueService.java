package com.freedy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.product.entity.SkuSaleAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 15:44:50
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuSaleAttrValueEntity> getAttrBySkuId(Long skuId);
}

