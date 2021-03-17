package com.freedy.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.freedy.common.utils.HttpUtils;
import com.freedy.mall.member.dao.MemberLevelDao;
import com.freedy.mall.member.exception.EmailExitException;
import com.freedy.mall.member.exception.UserNameExitException;
import com.freedy.mall.member.vo.MemberRegisterVo;
import com.freedy.mall.member.vo.SocialUser;
import com.freedy.mall.member.vo.SocialUserInfo;
import com.freedy.mall.member.vo.UserLoginVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.Query;

import com.freedy.mall.member.dao.MemberDao;
import com.freedy.mall.member.entity.MemberEntity;
import com.freedy.mall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo) {
        MemberEntity memberEntity = new MemberEntity();
        //检查用户名和邮箱是否唯一.为了让controller能感知异常，使用异常机制
        checkEmailUnique(vo.getEmail());
        checkUserNameUnique(vo.getUserName());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setEmail(vo.getEmail());
        //密码要进行加密存储
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = encoder.encode(vo.getPassword());
        memberEntity.setPassword(password);
        //设置默认等级
        Long levelId=memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelId);
        memberEntity.setStatus(1);
        memberEntity.setCreateTime(new Date());
        baseMapper.insert(memberEntity);
    }

    @Override
    public MemberEntity login(UserLoginVo vo) {
        MemberEntity member=baseMapper.queryAccountPassword(vo.getLoginAccount());
        String password=vo.getPassword();
        BCryptPasswordEncoder e = new BCryptPasswordEncoder();
        if (member!=null&&e.matches(password,member.getPassword())){
            //密码正确
            return member;
        }else {
            //密码错误或登录失败
            return null;
        }
    }

    @Override
    public MemberEntity login(SocialUser vo) throws Exception {
        Map<String,String> body=new HashMap<>();
        body.put("access_token",vo.getAccess_token());
        HttpResponse get = HttpUtils.doGet("https://gitee.com", "/api/v5/user",
                "get",new HashMap<>(), body);
        SocialUserInfo info = JSON.parseObject(EntityUtils.toString(get.getEntity()), SocialUserInfo.class);
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", info.getName()));
        if (memberEntity==null){
            //注册
            MemberEntity registerInfo = new MemberEntity();
            //设置默认等级
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(
                    () -> memberLevelDao.getDefaultLevel())
                    .thenAccept(registerInfo::setLevelId);
            registerInfo.setStatus(1);
            registerInfo.setUsername(info.getName());
            registerInfo.setCreateTime(new Date());
            registerInfo.setAccessToken(vo.getAccess_token());
            registerInfo.setRefreshToken(vo.getRefresh_token());
            registerInfo.setExpiresIn(vo.getExpires_in());
            future.get();
            baseMapper.insert(registerInfo);
            return registerInfo;
        }else {
            //登录
            MemberEntity update = new MemberEntity();
            BeanUtils.copyProperties(memberEntity,update);
            update.setAccessToken(vo.getAccess_token());
            update.setRefreshToken(vo.getRefresh_token());
            update.setExpiresIn(vo.getExpires_in());
            baseMapper.updateById(update);
            return update;
        }
    }

    private void checkEmailUnique(String email) throws EmailExitException{
        Integer count=baseMapper.emailCount(email);
        if (count!=0){
            throw new EmailExitException();
        }
    }

    private void checkUserNameUnique(String username) throws UserNameExitException{
        Integer count=baseMapper.userNameCount(username);
        if (count!=0){
            throw new UserNameExitException();
        }
    }

}