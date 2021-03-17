package com.freedy.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/13 21:18
 */
@Data
public class Cart {
    private List<CartItem> items;
    private Integer countNum;//商品数量
    private Integer countType;//商品类型数量
    private BigDecimal totalAmount=new BigDecimal("0");//商品总价
    private BigDecimal reduce = new BigDecimal("0");//见面方法

    public Integer getCountNum() {
        countNum = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                countNum += item.getCount();
            }
        }
        return countNum;
    }

    public BigDecimal getTotalAmount() {
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                if (item.getCheck()){
                    totalAmount = totalAmount.add(item.getTotalPrice());
                }
            }
        }
        totalAmount=totalAmount.subtract(reduce);
        return totalAmount;
    }

}
