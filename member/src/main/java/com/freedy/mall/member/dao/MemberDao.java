package com.freedy.mall.member.dao;

import com.freedy.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会员
 * 
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:32:06
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

    Integer emailCount(@Param("email") String email);

    Integer userNameCount(@Param("username") String username);

    MemberEntity queryAccountPassword(@Param("account") String account);
}
