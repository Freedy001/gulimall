package com.freedy.mall.product.vo;

import com.freedy.mall.product.entity.AttrEntity;
import com.freedy.mall.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/2/6 0:28
 */
@Data
public class AttrGroupWithAttrsVo extends AttrGroupEntity {
    private List<AttrEntity> attrs;
}
