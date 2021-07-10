package com.freedy.authserver.vo;

import lombok.Data;


/**
 * @author Freedy
 * @date 2021/3/10 22:23
 */
@Data
public class UserLoginVo {
    private String loginAccount;
    private String password;
}
