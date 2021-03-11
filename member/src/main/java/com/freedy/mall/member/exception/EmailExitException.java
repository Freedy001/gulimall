package com.freedy.mall.member.exception;

/**
 * @author Freedy
 * @date 2021/3/10 18:00
 */
public class EmailExitException extends RuntimeException {

    public EmailExitException(){
        super("手机号已存在!");
    }



}
