package com.freedy.mall.coupon.dao;

import com.freedy.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:25:29
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
