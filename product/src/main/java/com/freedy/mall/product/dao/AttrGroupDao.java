package com.freedy.mall.product.dao;

import com.freedy.mall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freedy.mall.product.vo.SkuItemVo;
import com.freedy.mall.product.vo.SpuItemAttrGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 15:44:51
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroup> getAttrGroupWithAttrsBySpuIdAndCatalogId(@Param("spuId") Long spuId,
                                                                    @Param("catalogId") Long catalogId);
}
