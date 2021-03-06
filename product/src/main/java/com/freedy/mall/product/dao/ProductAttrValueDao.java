package com.freedy.mall.product.dao;

import com.freedy.mall.product.entity.ProductAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * spu属性值
 * 
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 15:44:50
 */
@Mapper
public interface ProductAttrValueDao extends BaseMapper<ProductAttrValueEntity> {

    List<String> getSkuSaleAttrValuesAsStringList(@Param("skuId") String skuId);
}
