package com.chanper.gulimall.member.controller;

import com.chanper.common.exception.BizCodeEnum;
import com.chanper.common.utils.PageUtils;
import com.chanper.common.utils.R;
import com.chanper.gulimall.member.entity.MemberEntity;
import com.chanper.gulimall.member.exception.PhoneExistException;
import com.chanper.gulimall.member.exception.UserNameExistException;
import com.chanper.gulimall.member.feign.CouponFeignService;
import com.chanper.gulimall.member.service.MemberService;
import com.chanper.gulimall.member.vo.MemberLoginVo;
import com.chanper.gulimall.member.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 会员
 *
 * @author chanper
 * @email qianchaosolo@gmail.com
 * @date 2023-03-09 20:41:03
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;


    @PostMapping("/register")
    public R register(@RequestBody UserRegisterVo userRegisterVo) {
        try {
            memberService.register(userRegisterVo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UserNameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        MemberEntity memberEntity = memberService.login(vo);
        if (memberEntity != null) {
            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGINACTT_PASSWORD_ERROR.getCode(), BizCodeEnum.LOGINACTT_PASSWORD_ERROR.getMsg());
        }
    }

    @PostMapping("/giteeLogin")
    public R giteeLogin(@RequestParam("giteeInfo") String giteeInfo) throws Exception {
        MemberEntity member = memberService.giteeLogin(giteeInfo);
        return R.ok().setData(member);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
        public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
        public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
