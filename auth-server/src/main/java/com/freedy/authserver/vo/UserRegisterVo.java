package com.freedy.authserver.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author Freedy
 * @date 2021/3/9 22:00
 */
@Data
public class UserRegisterVo {
    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6,max = 18,message = "用户名必须是6~18位字符")
    private String userName;
    @NotEmpty(message = "密码必须提交")
    @Length(min = 6,max = 18,message = "密码必须是6~18位字符")
    private String password;
    @NotEmpty(message = "邮箱必须提交")
    @Pattern(regexp = "^([a-zA-Z0-9]+[_|\\_|\\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\\_|\\.]?)*[a-zA-Z0-9]+\\.[a-zA-Z]{2,3}$",
    message = "邮箱格式不正确")
    private String email;
    @NotEmpty(message = "验证码必须提交")
    @Length(min = 6,max = 6,message = "验证码必须是6位字符")
    private String code;
}
