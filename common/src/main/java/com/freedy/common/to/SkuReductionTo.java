package com.freedy.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Freedy
 * @date 2021/2/7 15:35
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPriceBean> memberPrice;
    @Data
    public static class MemberPriceBean implements Serializable {
        /**
         * id : 2
         * name : 铜牌会员
         * price : 7888
         */

        private Long id;
        private String name;
        private BigDecimal price;
    }
}
