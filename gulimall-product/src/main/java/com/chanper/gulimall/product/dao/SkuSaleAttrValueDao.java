package com.chanper.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chanper.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.chanper.gulimall.product.vo.ItemSaleAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author chanper
 * @email qianchaosolo@gmail.com
 * @date 2023-03-09 13:23:17
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {
    List<ItemSaleAttrVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);

    List<String> getSkuSaleAttrValuesAsStringList(@Param("skuId") Long skuId);

}
