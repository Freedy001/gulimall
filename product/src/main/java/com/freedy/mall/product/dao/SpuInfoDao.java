package com.freedy.mall.product.dao;

import com.freedy.mall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * spu信息
 * 
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 15:44:51
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {
    @Update("update pms_spu_info set publish_status=#{code},update_time=NOW() where id=#{spuId}")
    void updateSpuPublishStatus(@Param("spuId") Long spuId, @Param("code") int code);

    SpuInfoEntity getSpuInfoBySkuId(@Param("skuId") Long skuId);
}
