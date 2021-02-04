package com.freedy.mall.order.dao;

import com.freedy.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:39:24
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
