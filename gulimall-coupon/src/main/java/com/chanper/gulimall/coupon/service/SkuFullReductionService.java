package com.chanper.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chanper.common.to.SkuReductionTO;
import com.chanper.common.utils.PageUtils;
import com.chanper.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author chanper
 * @email qianchaosolo@gmail.com
 * @date 2023-03-09 20:15:34
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTO reductionTO);
}

