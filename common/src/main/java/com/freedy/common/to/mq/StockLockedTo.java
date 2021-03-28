package com.freedy.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/28 20:02
 */
@Data
public class StockLockedTo {
    private Long id;//库存工作单的id
    private Long detailId;//工作单详情的所有id

}
