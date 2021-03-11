package com.freedy.authserver.feign;

import com.freedy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Freedy
 * @date 2021/3/9 20:25
 */
@FeignClient("third-party")
public interface ThirdPartFeignService {
    @GetMapping("/email/{code}")
    R sendEmail(@PathVariable String code, @RequestParam("address") String address);
}
