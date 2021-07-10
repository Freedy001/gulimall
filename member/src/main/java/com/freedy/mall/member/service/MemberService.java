package com.freedy.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.member.entity.MemberEntity;
import com.freedy.mall.member.vo.MemberRegisterVo;
import com.freedy.mall.member.vo.SocialUser;
import com.freedy.mall.member.vo.UserLoginVo;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 会员
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:32:06
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);

    MemberEntity login(UserLoginVo vo);

    /**
     * 使用社交账号登录
     * 具有登录与注册的合并逻辑
     * @param vo
     * @return
     */
    MemberEntity login(SocialUser vo) throws Exception;
}

