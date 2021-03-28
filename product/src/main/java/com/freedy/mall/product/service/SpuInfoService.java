package com.freedy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.product.entity.SpuInfoDescEntity;
import com.freedy.mall.product.entity.SpuInfoEntity;
import com.freedy.mall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 15:44:51
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    void up(Long supId) throws Exception;

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

