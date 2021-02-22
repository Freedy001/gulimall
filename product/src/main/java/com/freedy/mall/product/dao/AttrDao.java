package com.freedy.mall.product.dao;

import com.freedy.mall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品属性
 * 
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 15:44:51
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> selectSearchAttrIds(@Param("attrId") List<Long> attrId);
}
