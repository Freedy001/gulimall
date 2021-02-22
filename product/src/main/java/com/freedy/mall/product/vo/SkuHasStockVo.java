package com.freedy.mall.product.vo;

import lombok.Data;

/**
 * @author Freedy
 * @date 2021/2/10 15:09
 */
@Data
public class SkuHasStockVo {
    private Long skuID;
    private boolean hasStock;
}
