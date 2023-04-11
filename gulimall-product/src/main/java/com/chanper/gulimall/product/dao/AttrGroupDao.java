package com.chanper.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chanper.gulimall.product.entity.AttrGroupEntity;
import com.chanper.gulimall.product.vo.SpuItemAttrGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 属性分组
 *
 * @author chanper
 * @email qianchaosolo@gmail.com
 * @date 2023-03-09 13:23:18
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {
    List<SpuItemAttrGroup> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}
