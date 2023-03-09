package com.chanper.gulimall.product.dao;

import com.chanper.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author chanper
 * @email qianchaosolo@gmail.com
 * @date 2023-03-09 13:23:18
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
