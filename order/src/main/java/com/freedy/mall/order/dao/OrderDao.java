package com.freedy.mall.order.dao;

import com.freedy.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freedy.mall.order.enume.OrderStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:39:24
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    void updateStatus(@Param("id") Long id, @Param("code") Integer code);

    void updateStatusByOrderSn(@Param("out_trade_no") String out_trade_no,
                               @Param("code") Integer code);

}
