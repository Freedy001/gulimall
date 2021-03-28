package com.freedy.mall.order.vo;

import com.freedy.mall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author Freedy
 * @date 2021/3/26 21:29
 */
@Data
public class SubmitOrderRespVo {
    private OrderEntity order;
    private Integer code;//状态码0代表成功
}
