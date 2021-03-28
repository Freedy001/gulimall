package com.freedy.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.order.entity.OrderEntity;
import com.freedy.mall.order.vo.OrderConfirmVo;
import com.freedy.mall.order.vo.OrderSubmitVo;
import com.freedy.mall.order.vo.SubmitOrderRespVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:39:24
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页返回需要用的数据
     * @return
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    /**
     * 下单
     */
    SubmitOrderRespVo submitOrder(OrderSubmitVo vo);

}

