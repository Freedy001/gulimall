package com.freedy.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/2/7 23:58
 */
@Data
public class PurchaseDoneVo {
    private Long id;
    private List<itemBean> items;

    @Data
    public static class itemBean{
        private Long itemId;
        private Integer status;
        private String reason;
    }
}
