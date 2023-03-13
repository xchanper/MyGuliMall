package com.chanper.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chanper.common.utils.PageUtils;
import com.chanper.common.utils.Query;
import com.chanper.gulimall.product.dao.CategoryDao;
import com.chanper.gulimall.product.entity.CategoryEntity;
import com.chanper.gulimall.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 2. 组装父子的树形结构

        return entities.stream().filter(
                categoryEntity -> categoryEntity.getParentCid() == 0
        ).peek((menu) -> menu.setChildren(getChildren(menu, entities))).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
    }

    /**
     * 递归查找所有菜单的子菜单
     *
     * @param root 一级菜单
     * @param all  全部菜单
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        // 1 使用全部的目录all首先找出传入目录root的所有下一层目录
        // 2 使用map对root的所有下一层目录 的 子目录进行设置
        // 3 利用sorted对所有下一层目录进行排序
        // 4  collect集合返回listWithTree()函数 这时候一个一级目录root的子目录已经设置成功

        return all.stream().filter(
                categoryEntity -> {
                    return categoryEntity.getParentCid() == root.getCatId();
                }
        ).peek(categoryEntity -> {
            // 设置子菜单
            categoryEntity.setChildren(getChildren(categoryEntity, all));
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
    }

}