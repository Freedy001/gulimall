package com.freedy.mall.member.exception;

/**
 * @author Freedy
 * @date 2021/3/10 18:00
 */
public class UserNameExitException extends RuntimeException{

    public UserNameExitException() {
        super("用户名已经存在!");
    }
}
