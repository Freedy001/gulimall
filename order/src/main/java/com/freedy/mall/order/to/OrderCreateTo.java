package com.freedy.mall.order.to;

import com.freedy.mall.order.entity.OrderEntity;
import com.freedy.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/26 22:05
 */
@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItem;
    private BigDecimal payPrice;
    private BigDecimal fare;
}
