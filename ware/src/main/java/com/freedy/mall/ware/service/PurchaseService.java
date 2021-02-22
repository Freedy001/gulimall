package com.freedy.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.ware.entity.PurchaseEntity;
import com.freedy.mall.ware.vo.MergeVo;
import com.freedy.mall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:45:34
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceiveList(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> ids);

    void done(PurchaseDoneVo doneVo);
}

