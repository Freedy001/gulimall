package com.freedy.mall.product.dao;

import com.freedy.mall.product.entity.SkuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku信息
 * 
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 15:44:50
 */
@Mapper
public interface SkuInfoDao extends BaseMapper<SkuInfoEntity> {

}
