package com.freedy.authserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.freedy.authserver.feign.MemberFeignService;
import com.freedy.authserver.feign.ThirdPartFeignService;
import com.freedy.authserver.service.LoginService;
import com.freedy.authserver.vo.UserRegisterVo;
import com.freedy.common.Exception.BizCodeEnum;
import com.freedy.common.constant.AuthServeConstant;
import com.freedy.common.utils.JsonBeautify;
import com.freedy.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Freedy
 * @date 2021/3/9 20:29
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public R SendVerificationCode(String address) {
        if (StringUtils.isEmpty(address)){
            return R.error(BizCodeEnum.EMAIL_CODE_NULL_EXCEPTION.getCode(),BizCodeEnum.EMAIL_CODE_NULL_EXCEPTION.getMsg());
        }
        String key= AuthServeConstant.EMAIL_CODE_CACHE_PREFIX+address;
        String s = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(s)||System.currentTimeMillis()-Long.parseLong(s.split("_")[1])>60000){
            //接口防刷，在后面加上当前系统时间做对比
            String code = UUID.randomUUID().toString().substring(0, 6);
            thirdPartFeignService.sendEmail(code, address);
            redisTemplate.opsForValue().set(key,code+"_"+System.currentTimeMillis(),30, TimeUnit.MINUTES);
            return R.ok();
        }else {
            return R.error(BizCodeEnum.EMAIL_CODE_EXCEPTION.getCode(),BizCodeEnum.EMAIL_CODE_EXCEPTION.getMsg());
        }
    }

    @Override
    public String register(UserRegisterVo registerVo) {
        String key=AuthServeConstant.EMAIL_CODE_CACHE_PREFIX + registerVo.getEmail();
        String codeVal = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(registerVo.getCode())||StringUtils.isEmpty(codeVal)){
            return "验证码Error";
        }
        String trueCode = codeVal.split("_")[0];
        if (!trueCode.equals(registerVo.getCode())){
            return "验证码不正确";
        }else {
            //删除验证码;令牌机制
            redisTemplate.delete(key);
            R r = memberFeignService.register(registerVo);
            if (r.getCode()==0){
                return "";
            }else {
                return (String) r.get("msg");
            }
        }
    }
}
