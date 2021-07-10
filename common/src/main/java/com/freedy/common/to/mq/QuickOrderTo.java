package com.freedy.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Freedy
 * @date 2021/4/1 21:15
 */
@Data
public class QuickOrderTo {
    private String orderSn;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer num;
    private Long promotionSessionId;
    private Long memberId;
}
