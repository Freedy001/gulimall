package com.freedy.mall.seckill.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀活动商品关联
 * 
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:25:28
 */
@Data
public class SeckillSkuRelationEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;
	/**
	 * 活动id
	 */
	private Long promotionId;
	/**
	 * 活动场次id
	 */
	private Long promotionSessionId;
	/**
	 * 商品id
	 */
	private Long skuId;
	/**
	 * 秒杀价格
	 */
	private BigDecimal seckillPrice;
	/**
	 * 秒杀总量
	 */
	private BigDecimal seckillCount;
	/**
	 * 每人限购数量
	 */
	private BigDecimal seckillLimit;
	/**
	 * 排序
	 */
	private Integer seckillSort;

}
