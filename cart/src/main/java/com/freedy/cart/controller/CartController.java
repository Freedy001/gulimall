package com.freedy.cart.controller;

import com.freedy.cart.interceptor.CartInterceptor;
import com.freedy.cart.service.CartService;
import com.freedy.cart.vo.Cart;
import com.freedy.cart.vo.CartItem;
import com.freedy.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 *
 *
 * @author Freedy
 * @date 2021/3/13 22:06
 */
@Controller
public class CartController {

    @Autowired
    CartService cartService;


    @ResponseBody
    @GetMapping("/currentItem")
    public List<CartItem> getItem(){
        return cartService.getUserCartItem();
    }


    @GetMapping("/cart")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Cart cart=cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    @GetMapping("/cart/check")
    public String check(@RequestParam("skuId")Long skuIdm){
        cartService.changeCheck(skuIdm);
        return "redirect:http://cart.freedymall.com/cart";
    }

    @GetMapping("/cart/count")
    public String count(Long skuId,Integer num){
        cartService.changeCount(skuId,num);
        return "redirect:http://cart.freedymall.com/cart";
    }
    @GetMapping("cart/del")
    public String del(Long skuId){
        cartService.delItem(skuId);
        return "redirect:http://cart.freedymall.com/cart";
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num)
            throws ExecutionException, InterruptedException {
        CartItem item = cartService.addToCart(skuId, num);
        return "redirect:http://cart.freedymall.com/success.html?skuId="+item.getSkuId();
    }

    @GetMapping("/success.html")
    public String successPage(Long skuId,Model model){
        CartItem cartItem=cartService.getCartItem(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }


}
