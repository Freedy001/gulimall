package com.freedy.mall.ware.dao;

import com.freedy.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商品库存
 * 
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:45:34
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    @Select("select sum(stock-stock_locked) from wms_ware_sku where sku_id=#{skuId}")
    Integer getStocksBySkuId(@Param("skuId") Long skuId);
}
