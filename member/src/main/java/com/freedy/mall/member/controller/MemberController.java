package com.freedy.mall.member.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import com.freedy.common.Exception.BizCodeEnum;
import com.freedy.mall.member.exception.EmailExitException;
import com.freedy.mall.member.exception.UserNameExitException;
import com.freedy.mall.member.feign.CouponFeignService;
import com.freedy.mall.member.vo.MemberRegisterVo;
import com.freedy.mall.member.vo.UserLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.freedy.mall.member.entity.MemberEntity;
import com.freedy.mall.member.service.MemberService;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.R;


/**
 * 会员
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:32:06
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo vo) {
        try {
            memberService.register(vo);
        } catch (EmailExitException e) {
            return R.error(BizCodeEnum.USER_EXIT_EXCEPTION.getCode(), BizCodeEnum.USER_EXIT_EXCEPTION.getMsg());
        } catch (UserNameExitException e) {
            return R.error(BizCodeEnum.EMAIL_EXIT_EXCEPTION.getCode(), BizCodeEnum.EMAIL_EXIT_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody UserLoginVo vo){
        MemberEntity login = memberService.login(vo);
        if (login==null){
            return R.error(BizCodeEnum.LOGIN_ACCOUNT_OR_PASSWORD_EXCEPTION.getCode(),
                    BizCodeEnum.LOGIN_ACCOUNT_OR_PASSWORD_EXCEPTION.getMsg());
        }
        return R.ok().setData(login);
    }

    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R r = couponFeignService.memberCoupon();
        return Objects.requireNonNull(R.ok().put("member", memberEntity)).put("coupons", r.get("coupon"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
