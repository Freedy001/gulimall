package com.freedy.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.freedy.mall.product.service.CategoryBrandRelationService;
import com.freedy.mall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.Query;

import com.freedy.mall.product.dao.CategoryDao;
import com.freedy.mall.product.entity.CategoryEntity;
import com.freedy.mall.product.service.CategoryService;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

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
        //查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //组装成父子的树形结构
        List<CategoryEntity> level1Menus = entities.stream().filter((categoryEntity) -> categoryEntity.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildren(menu, entities));
                    return menu;
                })
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //Todo 检查当前删除的菜单,是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCateLogPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        findParentPath(catelogId, path);
        Collections.reverse(path);
        return path.toArray(new Long[path.size()]);
    }

//    @Caching(evict = {
//            @CacheEvict(cacheNames = {"category"}, key = "'getLevel1Categories'"),
//            @CacheEvict(cacheNames = {"category"}, key = "'getCatelogJson'")
//    })
    @CacheEvict(cacheNames = {"category"},allEntries = true)
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    /**
     * 代表当前方法的结果需要缓存,如果缓存中有,方法不调用
     * 如果缓存中没有,会调用方法,最后将方法的结果放入缓存
     * 缓存使用jdk序列化机制
     * 缓存时间默认为永久
     *
     * @return
     */
    @Cacheable(cacheNames = {"category"}, key = "#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Cacheable(cacheNames = {"category"}, key = "#root.method.name",sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        return getCatelogJsonFromDb();
    }

    /**
     * 使用redisson实现分布式锁
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatelogJsonWithRedissonLock() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            //锁的名字 锁的粒度 越细越快
            RLock lock = redissonClient.getLock("CatelogJson-lock");
            lock.lock();//加锁
            System.out.println("获取分布式锁成功");
            Map<String, List<Catelog2Vo>> catelogJsonFromDb;
            try {
                catelogJsonFromDb = getCatelogJsonFromDb();
                //将对象转化为json存入到缓存中
                String jsonString = JSON.toJSONString(catelogJsonFromDb);
                redisTemplate.opsForValue().set("catalogJson", jsonString, 1, TimeUnit.DAYS);
            } finally {
                lock.unlock();//删锁
            }
            return catelogJsonFromDb;
        }
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    /**
     * 手动实现redis分布式锁
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatelogJsonWithRedisLock() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            String uuid = UUID.randomUUID().toString();
            Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
            if (lock == null || lock) {
                System.out.println("获取分布式锁成功");
                Map<String, List<Catelog2Vo>> catelogJsonFromDb;
                try {
                    catelogJsonFromDb = getCatelogJsonFromDb();
                    //将对象转化为json存入到缓存中
                    String jsonString = JSON.toJSONString(catelogJsonFromDb);
                    redisTemplate.opsForValue().set("catalogJson", jsonString, 1, TimeUnit.DAYS);
                } finally {
                    //删除锁  lua脚本进行原子操作
                    String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                            "then\n" +
                            "    return redis.call(\"del\",KEYS[1])\n" +
                            "else\n" +
                            "    return 0\n" +
                            "end";
                    redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                            Collections.singletonList("lock"), uuid);
                }
                return catelogJsonFromDb;
            } else {
                System.out.println("获取分布式锁失败,等待重试...");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getCatelogJson();
            }
        }
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    /**
     * 使用本地锁,防止多次查询数据库
     *
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatelogJsonWithLocalLock() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            synchronized (this) {
                Map<String, List<Catelog2Vo>> catelogJsonFromDb = getCatelogJsonFromDb();
                //将对象转化为json存入到缓存中
                String jsonString = JSON.toJSONString(catelogJsonFromDb);
                redisTemplate.opsForValue().set("catalogJson", jsonString, 1, TimeUnit.DAYS);
                return catelogJsonFromDb;
            }
        }
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    /**
     * 从数据库查询并封装分类业务
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatelogJsonFromDb() {
        //获取一边缓存，高并发时候的多次访问数据库
//        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
//        if (!StringUtils.isEmpty(catalogJson)) {
//            System.out.println("进入查数据库函数,命中缓存....");
//            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
//            });
//        }
        //将数据的多次查询变为一次
        List<CategoryEntity> allEntity = baseMapper.selectList(null);
        List<CategoryEntity> level1Categories = getLevel1Categories();
        return level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> entities = getNextLevelByCatId(allEntity, v.getCatId());
            List<Catelog2Vo> Catelog2Vo = new ArrayList<>();
            if (entities != null) {
                Catelog2Vo = entities.stream().map(item -> {
                    List<CategoryEntity> entitiesLevel3 = getNextLevelByCatId(allEntity, item.getCatId());
                    List<Catelog2Vo.Catelog3Vo> collect = entitiesLevel3.stream().map(i -> new Catelog2Vo.Catelog3Vo(item.getCatId().toString(), i.getCatId().toString(), i.getName())).collect(Collectors.toList());
                    return new Catelog2Vo(v.getCatId().toString(), item.getCatId().toString(), item.getName(), collect);
                }).collect(Collectors.toList());
            }
            return Catelog2Vo;
        }));

    }

    private List<CategoryEntity> getNextLevelByCatId(List<CategoryEntity> allEntity, Long catId) {
        return allEntity.stream().filter(item -> Objects.equals(item.getParentCid(), catId)).collect(Collectors.toList());
    }

    private void findParentPath(Long catelogId, List<Long> path) {
        //收集当前节点的id
        path.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), path);
        }
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .map(categoryEntity -> {
                    //找到子菜单
                    categoryEntity.setChildren(getChildren(categoryEntity, all));
                    return categoryEntity;
                })
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }

}