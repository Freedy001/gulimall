package com.freedy.mall.seckill.feign;

import com.freedy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Freedy
 * @date 2021/3/31 11:54
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/getLatest3DaySession")
    R getLatest3DaySession();

}
