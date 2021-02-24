package com.freedy.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Freedy
 * @date 2021/2/23 0:25
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catelog2Vo implements Serializable {

    /**
     * catalog1Id : 11
     * catalog3List : [{"catalog2Id":"61","id":"610","name":"商务休闲鞋"},{"catalog2Id":"61","id":"611","name":"正装鞋"}]
     * id : 61
     * name : 流行男鞋
     */

    private String catalog1Id;
    private String id;
    private String name;
    private List<Catelog3Vo> catalog3List;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catelog3Vo implements Serializable {
        /**
         * catalog2Id : 61
         * id : 610
         * name : 商务休闲鞋
         */

        private String catalog2Id;
        private String id;
        private String name;
    }
}
