package com.freedy.common.Exception;

/**
 * @author Freedy
 * @date 2021/2/3 15:52
 *
 * 错误码列表：
 * 10:通用
 *      001：参数格式校验
 *      002：邮箱验证码频率太高
 *      003：邮箱不能为空
 * 11:商品
 * 12:订单
 * 13:购物车
 * 14:物流
 * 15:用户
 *      001:用户存在
 *      002:邮箱存在
 *      003：账号密码错误
 * 21:库存
 */
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    EMAIL_CODE_EXCEPTION(10002,"邮箱验证码频率太高,请稍后再试"),
    EMAIL_CODE_NULL_EXCEPTION(10003,"邮箱不能为空！"),
    USER_EXIT_EXCEPTION(15001,"用户存在!"),
    EMAIL_EXIT_EXCEPTION(15002,"邮箱存在!"),
    LOGIN_ACCOUNT_OR_PASSWORD_EXCEPTION(15003,"账号密码错误!"),
    NO_STOCK_EXCEPTION(21001,"商品库存不足");

    private Integer code;
    private String msg;

    BizCodeEnum(Integer code,String msg){
        this.code=code;
        this.msg=msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
