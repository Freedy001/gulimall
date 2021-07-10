package com.freedy.mall.order.exception;

/**
 * @author Freedy
 * @date 2021/3/27 13:49
 */
public class NoStockException extends RuntimeException{
    public NoStockException() {
        super("商品库存不足！");
    }
}
