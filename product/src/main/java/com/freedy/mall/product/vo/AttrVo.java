package com.freedy.mall.product.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.freedy.mall.product.entity.AttrEntity;
import lombok.Data;

/**
 * @author Freedy
 * @date 2021/2/4 20:44
 */
@Data
public class AttrVo extends AttrEntity {
    private Long attrGroupId;
}
