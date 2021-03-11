package com.freedy.mall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author Freedy
 * @date 2021/3/9 22:00
 */
@Data
public class MemberRegisterVo {
    private String userName;
    private String password;
    private String email;
}
