package com.chanper.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chanper.common.utils.PageUtils;
import com.chanper.common.utils.Query;
import com.chanper.gulimall.product.dao.SpuImagesDao;
import com.chanper.gulimall.product.entity.SpuImagesEntity;
import com.chanper.gulimall.product.service.SpuImagesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuImagesService")
@Slf4j
public class SpuImagesServiceImpl extends ServiceImpl<SpuImagesDao, SpuImagesEntity> implements SpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuImagesEntity> page = this.page(
                new Query<SpuImagesEntity>().getPage(params),
                new QueryWrapper<SpuImagesEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveImages(Long id, List<String> images) {
        if (images == null || images.size() == 0) {
            log.warn("图片为空");
        } else {
            // 保存所有图片
            List<SpuImagesEntity> collect = images.stream().map(img -> {
                SpuImagesEntity imagesEntity = new SpuImagesEntity();
                imagesEntity.setSpuId(id);
                imagesEntity.setImgUrl(img);

                return imagesEntity;
            }).collect(Collectors.toList());
            this.saveBatch(collect);
        }
    }

}