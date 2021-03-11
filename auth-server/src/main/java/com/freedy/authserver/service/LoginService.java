package com.freedy.authserver.service;

import com.freedy.authserver.vo.UserRegisterVo;
import com.freedy.common.utils.R;

/**
 * @author Freedy
 * @date 2021/3/9 20:26
 */
public interface LoginService {

    /**
     * 通过邮箱发送验证码
     * @param address
     * @return
     */
    R SendVerificationCode(String address);

    String register(UserRegisterVo registerVo);
}
