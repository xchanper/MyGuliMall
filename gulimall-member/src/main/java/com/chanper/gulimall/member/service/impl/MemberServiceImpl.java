package com.chanper.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chanper.common.exception.BizCodeEnum;
import com.chanper.common.utils.HttpUtils;
import com.chanper.common.utils.PageUtils;
import com.chanper.common.utils.Query;
import com.chanper.gulimall.member.dao.MemberDao;
import com.chanper.gulimall.member.dao.MemberLevelDao;
import com.chanper.gulimall.member.entity.MemberEntity;
import com.chanper.gulimall.member.entity.MemberLevelEntity;
import com.chanper.gulimall.member.exception.PhoneExistException;
import com.chanper.gulimall.member.exception.UserNameExistException;
import com.chanper.gulimall.member.service.MemberService;
import com.chanper.gulimall.member.vo.MemberLoginVo;
import com.chanper.gulimall.member.vo.UserRegisterVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public void register(UserRegisterVo userRegisterVo) throws PhoneExistException, UserNameExistException {
        MemberEntity entity = new MemberEntity();

        // 1. 设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(memberLevelEntity.getId());

        // 2. 检查手机号、用户名是否唯一
        checkPhone(userRegisterVo.getPhone());
        checkUserName(userRegisterVo.getUserName());

        entity.setMobile(userRegisterVo.getPhone());
        entity.setUsername(userRegisterVo.getUserName());

        // 3. 密码加盐加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        entity.setPassword(bCryptPasswordEncoder.encode(userRegisterVo.getPassword()));
        // 4. 其它默认信息
        entity.setCity("湖南 长沙");
        entity.setCreateTime(new Date());
        entity.setStatus(0);
        entity.setNickname(userRegisterVo.getUserName());
        entity.setBirth(new Date());
        entity.setEmail("xxx@gmail.com");
        entity.setGender(1);
        entity.setJob("JAVA");
        baseMapper.insert(entity);
    }

    @Override
    public void checkPhone(String phone) throws PhoneExistException {
        if (this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone)) > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserName(String username) throws UserNameExistException {
        if (this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username)) > 0) {
            throw new UserNameExistException();
        }
    }


    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        // 去数据库查询
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (entity == null) {
            return null;
        } else {
            boolean matches = bCryptPasswordEncoder.matches(vo.getPassword(), entity.getPassword());
            if (matches) {
                // 不传递密码，仅在后台验证
                entity.setPassword(null);
                return entity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity giteeLogin(String giteeInfo) throws Exception {
        // 拿到 accesstoken，获取用户基本信息
        JSONObject baseJson = JSON.parseObject(giteeInfo);
        String accessToken = baseJson.getString("access_token");
        String expiresIn = baseJson.getString("expires_in");
        Map<String, String> params = new HashMap<>();
        params.put("access_token", baseJson.getString("access_token"));

        HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<>(), params);
        Assert.isTrue(response.getStatusLine().getStatusCode() == 200, String.valueOf(BizCodeEnum.SOCIALUSER_LOGIN_ERROR));

        String s = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = JSON.parseObject(s);
        String id = jsonObject.getString("id");

        MemberEntity member = this.getOne(new QueryWrapper<MemberEntity>().eq("social_uid", "gitee_" + id));
        if (member != null) {
            // 已经注册过，更新令牌、过期时间
            MemberEntity newMember = new MemberEntity();
            newMember.setId(member.getId());
            newMember.setAccessToken(accessToken);
            newMember.setExpiresIn(expiresIn);
            this.updateById(member);
            return member;
        } else {
            // 第一次授权登录，需要注册
            MemberEntity newMember = new MemberEntity();
            newMember.setSocialUid("gitee_" + id);
            newMember.setNickname(jsonObject.getString("name"));
            newMember.setAccessToken(accessToken);
            newMember.setExpiresIn(expiresIn);
            this.save(newMember);
            return newMember;
        }
    }
}