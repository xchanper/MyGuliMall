package com.chanper.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chanper.common.utils.PageUtils;
import com.chanper.gulimall.product.entity.SpuInfoEntity;
import com.chanper.gulimall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author chanper
 * @email qianchaosolo@gmail.com
 * @date 2023-03-09 13:23:17
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo saveVo);

    void saveBatchSpuInfo(SpuInfoEntity spuInfoEntity);

    /**
     * SPU模糊查询
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    void up(Long spuId);

    /**
     * 返回一个SpuEntity
     */
    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

