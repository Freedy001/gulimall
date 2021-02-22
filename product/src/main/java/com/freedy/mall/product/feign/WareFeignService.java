package com.freedy.mall.product.feign;

import com.freedy.common.utils.R;
import com.freedy.mall.product.vo.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/2/10 15:23
 */
@FeignClient("mall-ware")
public interface WareFeignService {


    @PostMapping("/ware/waresku/hasstock")
    R<List<SkuHasStockVo>> getSkusHasStock(@RequestBody List<Long> SkuIds);
}
