package com.freedy.mall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/7 13:31
 */
@ToString
@Data
public class SpuItemAttrGroup {
    private String groupName;
    private List<SkuBaseAttrVo> attrs;
}
