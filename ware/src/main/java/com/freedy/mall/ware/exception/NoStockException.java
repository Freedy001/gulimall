package com.freedy.mall.ware.exception;

/**
 * @author Freedy
 * @date 2021/3/27 11:12
 */
public class NoStockException extends RuntimeException{
    public NoStockException(Long skuId) {
        super("商品id:"+skuId+"没有足够的库存了");
    }

}
