package com.freedy.mall.ware.vo;

import com.freedy.mall.ware.service.impl.WareSkuServiceImpl;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/13 21:19
 */
@Data
public class OrderItemVo {
    private Long skuId;
    private Boolean check=true;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    // TODO: 2021/3/22 查询状态
    private Boolean hasStock=true;
    private BigDecimal weight=new BigDecimal("1");

    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(""+count));
    }
}
