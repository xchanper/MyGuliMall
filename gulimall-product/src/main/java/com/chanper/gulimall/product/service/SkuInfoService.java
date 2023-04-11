package com.chanper.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chanper.common.utils.PageUtils;
import com.chanper.gulimall.product.entity.SkuInfoEntity;
import com.chanper.gulimall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author chanper
 * @email qianchaosolo@gmail.com
 * @date 2023-03-09 13:23:17
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    PageUtils queryPageByCondition(Map<String, Object> params);

    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;
}

