package com.chanper.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chanper.common.utils.PageUtils;
import com.chanper.common.utils.Query;
import com.chanper.gulimall.product.dao.CategoryDao;
import com.chanper.gulimall.product.entity.CategoryEntity;
import com.chanper.gulimall.product.service.CategoryBrandRelationService;
import com.chanper.gulimall.product.service.CategoryService;
import com.chanper.gulimall.product.vo.Catalog3Vo;
import com.chanper.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

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
        // 1 查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 2 组装成父子的树型结构
        // 2.1 找到所有一级分类
        List<CategoryEntity> level1Menus = categoryEntities.stream().filter(
                // 找到一级
                categoryEntity -> categoryEntity.getParentCid() == 0
        ).map(menu -> {
            // 把当前的child属性改了之后重新返回
            menu.setChildren(getChildren(menu, categoryEntities));
            return menu;
        }).sorted((menu1, menu2) ->
                menu1.getSort() - menu2.getSort()).collect(Collectors.toList());

        return level1Menus;
    }

    /**
     * 找到一个三级目录的完整路径
     *
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        findParentPath(catelogId, paths);
        // 收集的时候是顺序 前端是逆序显示的 所以用集合工具类给它逆序一下
        Collections.reverse(paths);
        return paths.toArray(new Long[0]);
    }

    /**
     * 级联更新所有数据 [分区名默认是就是缓存的前缀] SpringCache: 不加锁
     *
     * @CacheEvict: 缓存失效模式 --- 页面一修改 然后就清除这两个缓存
     * key = "'getLevel1Categorys'" : 记得加单引号 [子解析字符串]
     * @Caching: 同时进行多种缓存操作
     * @CacheEvict(value = {"category"}, allEntries = true) : 删除这个分区所有数据
     * @CachePut: 这次查询操作写入缓存
     */
    @CacheEvict(value = {"category"}, allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Override
    public Long[] findCateLogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        paths = findParentPath(catelogId, paths);
        // 收集的时候是顺序 前端是逆序显示的 所以用集合工具类给它逆序一下
        Collections.reverse(paths);
        return paths.toArray(new Long[0]);
    }

    /**
     * 递归收集所有父节点
     *
     * @param catelogId
     * @param paths
     * @return
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 获取某一个菜单的子菜单
     * 在all里找root的子菜单
     *
     * @param
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream().filter(categoryEntity -> {
            // 找到当前id的子菜单
            return categoryEntity.getParentCid() == root.getCatId();
        }).peek(categoryEntity -> {
            // 1 找到子菜单，递归找法
            categoryEntity.setChildren(getChildren(categoryEntity, all));
        }).sorted(Comparator.comparingInt(CategoryEntity::getSort)).collect(Collectors.toList());
    }

    /**
     * @Cacheable: 当前方法的结果需要缓存 并指定缓存名字
     * 缓存的value值 默认使用jdk序列化
     * 默认ttl时间 -1
     * key: 里面默认会解析表达式 字符串用 ''
     * <p>
     * 自定义:
     * 1.指定生成缓存使用的key
     * 2.指定缓存数据存活时间	[配置文件中修改]
     * 3.将数据保存为json格式
     * <p>
     * sync = true: --- 开启同步锁
     */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        // System.out.println("CategoryServiceImpl::getCatelogJSON()...");
        List<CategoryEntity> entityList = baseMapper.selectList(null);
        // 查询所有一级分类
        List<CategoryEntity> level1 = getCategoryEntities(entityList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 拿到每一个一级分类 然后查询他们的二级分类
            List<CategoryEntity> entities = getCategoryEntities(entityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                    // 找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getCategoryEntities(entityList, l2.getCatId());
                    // 三级分类有数据的情况下
                    if (level3 != null) {
                        List<Catalog3Vo> catalog3Vos = level3.stream().map(l3 -> new Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString())).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }


    /**
     * 第一次查询的所有 CategoryEntity 然后根据 parent_cid去这里找
     */
    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> entityList, Long parent_cid) {
        return entityList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
    }

    /**
     * redis无缓存 查询数据库
     */
    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        String catelogJSON = stringRedisTemplate.opsForValue().get("catelogJSON");
        if (!StringUtils.isEmpty(catelogJSON)) {
            return JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
        // 优化：将查询变为一次
        List<CategoryEntity> entityList = baseMapper.selectList(null);

        // 查询所有一级分类
        List<CategoryEntity> level1 = getCategoryEntities(entityList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 拿到每一个一级分类 然后查询他们的二级分类
            List<CategoryEntity> entities = getCategoryEntities(entityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                    // 找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getCategoryEntities(entityList, l2.getCatId());
                    // 三级分类有数据的情况下
                    if (level3 != null) {
                        List<Catalog3Vo> catalog3Vos = level3.stream().map(l3 -> new Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString())).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        // 优化：查询到数据库就再锁还没结束之前放入缓存
        stringRedisTemplate.opsForValue().set("catelogJSON", JSON.toJSONString(parent_cid), 1, TimeUnit.DAYS);
        return parent_cid;
    }

    /**
     * redis没有数据 查询DB [本地锁解决方案]
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithLocalLock() {
        synchronized (this) {
            // 双重检查 是否有缓存
            return getDataFromDB();
        }
    }


    /**
     * 分布式锁
     * 基于 Redis + lua脚本
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedisLock() {
        // 1. set lock uuid nx ex 设置分布式锁以及过期时间
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);

        if (lock) {
            // 2. 获取数据
            Map<String, List<Catelog2Vo>> data;
            try {
                data = getDataFromDB();
            } finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // 3. Lua 脚本执行原子查询和删除 [分布式下必须是Lua脚本删锁,不然会因为业务处理时间、网络延迟等等引起数据还没返回锁过期或者返回的过程中过期 然后把别人的锁删了]
                stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList("lock"), uuid);
            }
            return data;
        } else {
            // 获取锁失败自旋
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatelogJsonFromDBWithRedisLock();
        }
    }

    /**
     * redisson 微服务集群锁
     * 缓存中的数据如何与数据库保持一致
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedissonLock() {
        RLock lock = redissonClient.getLock("CatelogJson-lock");
        lock.lock();

        Map<String, List<Catelog2Vo>> data;
        try {
            data = getDataFromDB();
        } finally {
            lock.unlock();
        }
        return data;
    }
}