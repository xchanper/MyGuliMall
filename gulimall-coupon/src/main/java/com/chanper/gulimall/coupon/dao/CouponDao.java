package com.chanper.gulimall.coupon.dao;

import com.chanper.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author chanper
 * @email qianchaosolo@gmail.com
 * @date 2023-03-09 20:15:36
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
