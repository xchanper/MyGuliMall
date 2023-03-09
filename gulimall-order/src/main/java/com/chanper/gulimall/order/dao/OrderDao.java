package com.chanper.gulimall.order.dao;

import com.chanper.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author chanper
 * @email qianchaosolo@gmail.com
 * @date 2023-03-09 20:50:32
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
