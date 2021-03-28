package com.freedy.mall.order.feign;

import com.freedy.mall.order.vo.MemberAddressVo;
import com.freedy.mall.order.vo.MemberReceiveAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author Freedy
 * @date 2021/3/21 21:47
 */
@FeignClient("mall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    List<MemberAddressVo> getAddress(@PathVariable Long memberId);

    @GetMapping("/member/memberreceiveaddress/{Id}/singleaddress")
    MemberReceiveAddressVo getSingleAddress(@PathVariable Long Id);

}
