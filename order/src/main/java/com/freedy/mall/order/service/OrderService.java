package com.freedy.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:39:24
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

