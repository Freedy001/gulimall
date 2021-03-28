package com.freedy.mall.order.feign;

import com.freedy.mall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/21 22:22
 */
@FeignClient("mall-cart")
public interface CartFeignService {

    @GetMapping("/currentItem")
    List<OrderItemVo> getItem();

}
