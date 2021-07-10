package com.freedy.mall.ware.vo;

import lombok.Data;

/**
 * @author Freedy
 * @date 2021/3/27 10:30
 */
@Data
public class LockStockResult {
    private long skuId;
    private Integer count;
    private boolean locked;
}
