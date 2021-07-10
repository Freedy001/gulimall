package com.freedy.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/21 21:34
 */
@Data
public class OrderConfirmVo {
    private List<MemberAddressVo> address;
    private List<OrderItemVo> items;
    private Integer integration;
    private BigDecimal payPrice;

    private String orderToken;

    public Integer getCount(){
        Integer count=0;
        if (items != null) {
            for (OrderItemVo item : items) {
                count+=item.getCount();
            }
        }
        return count;
    }

    public BigDecimal getTotal() {
        BigDecimal decimal = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                decimal = decimal.add(
                        item.getPrice().multiply(
                                new BigDecimal(item.getCount()+"")
                        )
                );
            }
        }
        return decimal;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
