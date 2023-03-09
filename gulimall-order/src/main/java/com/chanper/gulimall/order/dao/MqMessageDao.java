package com.chanper.gulimall.order.dao;

import com.chanper.gulimall.order.entity.MqMessageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author chanper
 * @email qianchaosolo@gmail.com
 * @date 2023-03-09 20:50:32
 */
@Mapper
public interface MqMessageDao extends BaseMapper<MqMessageEntity> {
	
}
