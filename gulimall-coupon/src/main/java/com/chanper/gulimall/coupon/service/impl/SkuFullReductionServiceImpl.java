package com.chanper.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chanper.common.to.MemberPrice;
import com.chanper.common.to.SkuReductionTO;
import com.chanper.common.utils.PageUtils;
import com.chanper.common.utils.Query;
import com.chanper.gulimall.coupon.dao.SkuFullReductionDao;
import com.chanper.gulimall.coupon.entity.MemberPriceEntity;
import com.chanper.gulimall.coupon.entity.SkuFullReductionEntity;
import com.chanper.gulimall.coupon.entity.SkuLadderEntity;
import com.chanper.gulimall.coupon.service.MemberPriceService;
import com.chanper.gulimall.coupon.service.SkuFullReductionService;
import com.chanper.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired // 多件折扣
    private SkuLadderService skuLadderService;

    @Autowired // 会员价
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * sku的多件折扣、满减、会员价等信息；gulimall_sms->sms_sku_ladder | sms_sku_full_reduction | sms_member_price
     *
     * @param skuReductionTO
     */
    @Override
    public void saveSkuReduction(SkuReductionTO skuReductionTO) {
        // 1. sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTO.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTO.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTO.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTO.getCountStatus());
        if (skuReductionTO.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }
        skuLadderService.save(skuLadderEntity);

        // 2、sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTO, skuFullReductionEntity);
        if ((skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0")) > 0)) {
            this.save(skuFullReductionEntity);
        }

        // 3. sms_member_price
        List<MemberPrice> memberPrice = skuReductionTO.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(m -> {
            MemberPriceEntity priceEntity = new MemberPriceEntity();
            priceEntity.setSkuId(skuReductionTO.getSkuId());
            priceEntity.setMemberLevelId(m.getId());
            priceEntity.setMemberLevelName(m.getName());
            priceEntity.setMemberPrice(m.getPrice());
            priceEntity.setAddOther(1);

            return priceEntity;
        }).filter(item -> (item.getMemberPrice().compareTo(new BigDecimal("0")) > 0)
        ).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }

}