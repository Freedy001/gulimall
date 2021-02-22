package com.freedy.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购细节
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:45:34
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);
}

