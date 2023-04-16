package com.chanper.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chanper.common.utils.PageUtils;
import com.chanper.gulimall.member.entity.MemberEntity;
import com.chanper.gulimall.member.exception.PhoneExistException;
import com.chanper.gulimall.member.exception.UserNameExistException;
import com.chanper.gulimall.member.vo.MemberLoginVo;
import com.chanper.gulimall.member.vo.UserRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author chanper
 * @email qianchaosolo@gmail.com
 * @date 2023-03-09 20:41:03
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterVo userRegisterVo) throws PhoneExistException, UserNameExistException;

    void checkPhone(String phone) throws PhoneExistException;

    void checkUserName(String username) throws UserNameExistException;

    /**
     * 普通登录
     *
     * @param vo
     * @return
     */
    MemberEntity login(MemberLoginVo vo);

    MemberEntity giteeLogin(String giteeInfo) throws Exception;
}

