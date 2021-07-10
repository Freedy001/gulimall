package com.freedy.mall.product.vo;

import lombok.Data;

/**
 * @author Freedy
 * @date 2021/2/4 21:17
 */

@Data
public class AttrRespVo extends AttrVo {
    private String catelogName;

    private String groupName;

    private Long[] catelogPath ;
}
