package com.freedy.authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.freedy.authserver.feign.MemberFeignService;
import com.freedy.authserver.service.LoginService;
import com.freedy.common.constant.AuthServeConstant;
import com.freedy.common.to.MemberEntity;
import com.freedy.authserver.vo.UserLoginVo;
import com.freedy.authserver.vo.UserRegisterVo;
import com.freedy.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Freedy
 * @date 2021/3/9 20:22
 */
@Controller
public class LoginController {

    @Autowired
    LoginService loginService;

    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/email/sendcode")
    public R sendMsg(@RequestParam("address") String address){
        return loginService.SendVerificationCode(address);
    }

    /**
     * //todo 分布式下的session问题
     * RedirectAttributes 模拟重定向携带数据
     * @param registerVo
     * @param result
     * @param model
     * @return
     */
    @PostMapping("/register/postData")
    public String register(@Valid UserRegisterVo registerVo, BindingResult result, RedirectAttributes model){
        if (result.hasErrors()){
            //校验失败重定向到reg
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            model.addFlashAttribute("errors",errors);
            return "redirect:http://auth.freedymall.com/reg";
        }
        try {
            String register = loginService.register(registerVo);
            if (!StringUtils.isEmpty(register)){
                return error(model, register);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return error(model,"注册失败，请稍后重试!");
        }
        return "redirect:http://auth.freedymall.com/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServeConstant.LOGIN_USER);
        if (attribute==null){
            return "login";
        }else {
            return "redirect:http://freedymall.com/";
        }
    }

    @PostMapping("/account/login")
    public String login(UserLoginVo vo, RedirectAttributes model, HttpSession session){
        R login = memberFeignService.login(vo);
        if (login.getCode()==0){
            MemberEntity data = login.getData(new TypeReference<MemberEntity>(){});
            session.setAttribute(AuthServeConstant.LOGIN_USER,data);
            return "redirect:http://freedymall.com/";
        }else {
            model.addFlashAttribute("error",login.getData("msg",new TypeReference<String>(){}));
            return "redirect:http://auth.freedymall.com/login";
        }
    }

    private String error(RedirectAttributes model, String reason) {
        Map<String, String> map=new HashMap<>();
        map.put("reason",reason);
        model.addFlashAttribute("error",map);
        return "redirect:http://auth.freedymall.com/reg";
    }
}
