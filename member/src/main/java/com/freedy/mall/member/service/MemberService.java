package com.freedy.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.member.entity.MemberEntity;

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
}

