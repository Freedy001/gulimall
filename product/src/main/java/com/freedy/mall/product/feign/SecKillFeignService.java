package com.freedy.mall.product.feign;

import com.freedy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Freedy
 * @date 2021/3/31 21:42
 */
@FeignClient("mall-seckill")
public interface SecKillFeignService {

    @GetMapping("/seckill/{skuId}")
    R getSkuSecKillInfo(@PathVariable Long skuId);
}
