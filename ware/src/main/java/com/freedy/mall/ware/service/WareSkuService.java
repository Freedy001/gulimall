package com.freedy.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.ware.entity.WareSkuEntity;
import com.freedy.mall.ware.vo.SkuHasStockVo;
import com.freedy.mall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:45:34
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    /**
     * 为某个订单锁定库存
     * @return
     */
    boolean orderLockStock(WareSkuLockVo vo);

    boolean orderLockStockAuto(WareSkuLockVo vo);

    void releaseLockStock();
}

