package com.freedy.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.freedy.authserver.feign.MemberFeignService;
import com.freedy.common.constant.AuthServeConstant;
import com.freedy.common.to.MemberEntity;
import com.freedy.authserver.vo.SocialUser;
import com.freedy.common.utils.HttpUtils;
import com.freedy.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录请求
 * @author Freedy
 * @date 2021/3/12 13:34
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/gitee")
    public String gitee(@RequestParam("code") String code, HttpSession session) throws Exception {
        //根据code换取accessToken
        Map<String,String> body=new HashMap<>();
        body.put("grant_type","authorization_code");
        body.put("code",code);
        body.put("client_id","80fabe153dbc1a83b67a8975a975e7baeb9ba775c340d6478e1feca8d6027a09");
        body.put("client_secret","d31b4674485e42bcc3323cbe41d2b8cfe9e1d6b36d0e446640e3319f552b47e7");
        body.put("redirect_uri","http://auth.freedymall.com/oauth2.0/gitee");
        Map<String,String> header=new HashMap<>();
        HttpResponse post = HttpUtils.doPost("https://gitee.com", "/oauth/token",
                "post",header, null, body);
        //处理
        if (post.getStatusLine().getStatusCode()==200){
            String json = EntityUtils.toString(post.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            //1、当前用户第一次进网站的时候，自动注册进来
            // 为当前社交用户自动生成一个会员信息，以后这个社交账号就对应指定的会员
            R r = memberFeignService.oauth2Login(socialUser);
            if (r.getCode()==0){
                MemberEntity data = r.getData(new TypeReference<MemberEntity>() {
                });
                //发送cookie的时候要指定域名为父域名，即使是子域系统发的卡，也能让父域使用
                //todo 1.默认发送的令牌是session=xxxxxx 作用域:当前域（解决子域session共享问题）
                // TODO: 2021/3/13 使用json序列化方式来序列化对象数据到redis中
                session.setAttribute(AuthServeConstant.LOGIN_USER,data);
                log.info("登录成功:用户:{}",data);
                //登录成功跳回首页
                return "redirect:http://freedymall.com";
            }else {
                return "redirect:http://auth.freedymall.com/login";
            }
        }else {
            return "redirect:http://auth.freedymall.com/login";
        }
    }
}
