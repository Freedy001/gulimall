package com.freedy.mall.order.interceptor;

import com.freedy.common.constant.AuthServeConstant;
import com.freedy.common.to.MemberEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Freedy
 * @date 2021/3/21 21:15
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberEntity> loginUser=new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //http://order.freedymall.com/order/order/status/123
        if (new AntPathMatcher().match("/order/order/status/**",request.getRequestURI())){
            return true;
        }
        if (new AntPathMatcher().match("/payed/**",request.getRequestURI())){
            return true;
        }
        MemberEntity attribute =(MemberEntity) request.getSession().getAttribute(AuthServeConstant.LOGIN_USER);
        if (attribute!=null){
            loginUser.set(attribute);
            return true;
        }else {
            request.getSession().setAttribute("msg","请先进行登录！");
            response.sendRedirect("http://auth.freedymall.com/login");
            return false;
        }
    }
}
