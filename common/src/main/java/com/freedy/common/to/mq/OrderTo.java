package com.freedy.common.to.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Freedy
 * @date 2021/3/29 16:38
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderTo {
    /**
     * 订单号
     */
    private String orderSn;

}
