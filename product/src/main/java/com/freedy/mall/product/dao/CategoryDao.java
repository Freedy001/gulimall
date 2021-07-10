package com.freedy.mall.product.dao;

import com.freedy.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 15:44:51
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {

}
