package com.freedy.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/24 22:27
 */
@Data
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType;
    //无需提交购买的商品直接去购物车在获取一边
    //优惠发票等信息
    private String orderToken;//放重令牌
    private BigDecimal payPrice;
}
