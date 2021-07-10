package com.freedy.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.freedy.cart.feign.ProductFeignService;
import com.freedy.cart.interceptor.CartInterceptor;
import com.freedy.cart.service.CartService;
import com.freedy.cart.vo.Cart;
import com.freedy.cart.vo.CartItem;
import com.freedy.cart.vo.SkuInfoVo;
import com.freedy.cart.vo.UserInfoTo;
import com.freedy.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author Freedy
 * @date 2021/3/13 21:51
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor pool;

    private final String CART_PREFIX = "freedyMall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        String o = (String) ops.get(skuId.toString());
        if (StringUtils.isEmpty(o)) {
            //远程查询当前要添加的商品的信息
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> skuInfoTask = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.info(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(data.getPrice());
            }, pool);
            //查询sku组合信息
            CompletableFuture<Void> skuAttrTask = CompletableFuture.runAsync(() -> {
                List<String> attr = productFeignService.getSkuSaleAttrValues(String.valueOf(skuId));
                cartItem.setSkuAttr(attr);
            }, pool);
            skuInfoTask.get();
            skuAttrTask.get();
            String cartItemJson = JSON.toJSONString(cartItem);
            ops.put(skuId.toString(), cartItemJson);
            return cartItem;
        } else {
            CartItem item = JSON.parseObject(o, CartItem.class);
            item.setCount(item.getCount() + num);
            String jsonString = JSON.toJSONString(item);
            ops.put(skuId.toString(), jsonString);
            return item;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        String o = (String) ops.get(skuId.toString());
        return JSON.parseObject(o, CartItem.class);
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            String key = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempList = getCartItemList(key);
            if (tempList != null) {
                for (CartItem item : tempList) {
                    addToCart(item);
                }
                clearCart(key);
            }
            String id = CART_PREFIX + userInfoTo.getUserId();
            cart.setItems(getCartItemList(id));
        } else {
            String userKey = CART_PREFIX + userInfoTo.getUserKey();
            cart.setItems(getCartItemList(userKey));
        }
        return cart;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void changeCheck(Long skuId) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        String o = (String) ops.get(skuId.toString());
        CartItem item = JSON.parseObject(o, CartItem.class);
        if (item != null) {
            item.setCheck(!item.getCheck());
            addToCart(item);
        }
    }

    @Override
    public void changeCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        String o = (String) ops.get(skuId.toString());
        CartItem item = JSON.parseObject(o, CartItem.class);
        if (item != null) {
            item.setCount(num);
            addToCart(item);
        }
    }

    @Override
    public void delItem(Long skuId) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        ops.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItem() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId()==null){
            return null;
        }else{
            //noinspection ConstantConditions
            return getCartItemList(CART_PREFIX + userInfoTo.getUserId())
                    .stream().peek(item->{
                        BigDecimal price = productFeignService.getPrice(item.getSkuId());
                        item.setPrice(price);
                    }).filter(CartItem::getCheck).collect(Collectors.toList());
        }
    }

    private void addToCart(CartItem item) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        ops.put(item.getSkuId().toString(), JSON.toJSONString(item));
    }

    /**
     * 获取到我们要操作的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            //登陆了
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        return redisTemplate.boundHashOps(cartKey);
    }

    private List<CartItem> getCartItemList(String key) {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(key);
        List<Object> values = ops.values();
        if (values != null && values.size() > 0) {
            return values.stream().map(obj -> JSON.parseObject((String) obj, CartItem.class)).collect(Collectors.toList());
        }
        return null;
    }

}
