package com.chanper.gulimall.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chanper.gulimall.member.entity.MemberLevelEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 *
 * @author chanper
 * @email qianchaosolo@gmail.com
 * @date 2023-03-09 20:41:03
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    MemberLevelEntity getDefaultLevel();
}
