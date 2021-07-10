package com.freedy.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Freedy
 * @date 2021/2/7 15:17
 */
@Data
public class SpuBoundTo {
    private Long SpuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
