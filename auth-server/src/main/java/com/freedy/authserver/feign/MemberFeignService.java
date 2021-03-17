package com.freedy.authserver.feign;

import com.freedy.authserver.vo.SocialUser;
import com.freedy.authserver.vo.UserLoginVo;
import com.freedy.authserver.vo.UserRegisterVo;
import com.freedy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Freedy
 * @date 2021/3/10 19:12
 */
@FeignClient("mall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauth2Login(@RequestBody SocialUser vo);
}
