package com.freedy.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/27 9:42
 */
@Data
public class WareSkuLockVo {

    private String orderSn;//订单号
    private List<OrderItemVo> locks;//需要锁住的所有信息

}
