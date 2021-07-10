package com.freedy.mall.product.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freedy.mall.product.entity.CategoryBrandRelationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 品牌分类关联
 * 
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-11-17 21:25:25
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    @Update("update pms_category_brand_relation set catelog_name=#{name} where catelog_id=#{catId}")
    void updateCategory(@Param("catId") Long catId, @Param("name") String name);
}
