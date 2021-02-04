package com.freedy.mall.member.feign;

import com.freedy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Freedy
 * @date 2021/1/30 23:38
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/categorybounds/member/list")
    public R memberCoupon();

}
