package com.freedy.cart.service;

import com.freedy.cart.vo.Cart;
import com.freedy.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Freedy
 * @date 2021/3/13 21:51
 */
public interface CartService {
    /**
     * 将商品添加到购物车
     * @param skuId
     * @param num
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 获取购物车的某个购物项
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void clearCart(String cartKey);

    void changeCheck(Long skuIdm);

    void changeCount(Long skuId, Integer num);

    void delItem(Long skuId);

    List<CartItem> getUserCartItem();
}
