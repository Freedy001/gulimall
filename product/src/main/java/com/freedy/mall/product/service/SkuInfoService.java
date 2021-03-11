package com.freedy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.product.entity.SkuInfoEntity;
import com.freedy.mall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 15:44:50
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(Long supId);

    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;
}

