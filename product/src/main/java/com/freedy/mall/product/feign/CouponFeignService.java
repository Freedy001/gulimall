package com.freedy.mall.product.feign;

import com.freedy.common.to.SkuReductionTo;
import com.freedy.common.to.SpuBoundTo;
import com.freedy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * @author Freedy
 * @date 2021/2/7 15:11
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {

    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R savaSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
