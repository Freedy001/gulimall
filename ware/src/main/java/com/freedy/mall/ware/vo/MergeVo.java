package com.freedy.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/2/7 22:45
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
