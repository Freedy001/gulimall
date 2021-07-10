package com.freedy.cart.interceptor;

import com.freedy.cart.vo.UserInfoTo;
import com.freedy.common.constant.AuthServeConstant;
import com.freedy.common.constant.CartConstant;
import com.freedy.common.to.MemberEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 再执行方法之前，判断用户的登陆状态
 * 并封装传递给controller目标请求
 * @author Freedy
 * @date 2021/3/13 22:17
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal=new ThreadLocal<>();

    /**
     * 在目标方法执行之前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        MemberEntity loginUser =(MemberEntity)request.getSession().getAttribute(AuthServeConstant.LOGIN_USER);
        if (loginUser!=null){
            //登录了
            userInfoTo.setUserId(loginUser.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies!=null&&cookies.length>0){
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)){
                    userInfoTo.setUserKey(cookie.getValue());
                    cookie.setDomain("freedymall.com");
                    cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
        //如果没有临时用户要分配一个临时用户
        if (!StringUtils.hasLength(userInfoTo.getUserKey())){
            String s = UUID.randomUUID().toString();
            userInfoTo.setUserKey(s);
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME,s);
            cookie.setDomain("freedymall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
        threadLocal.set(userInfoTo);
        return true;
    }

}
