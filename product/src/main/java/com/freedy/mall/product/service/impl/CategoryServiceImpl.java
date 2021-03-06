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
        //??????????????????
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //??????????????????????????????
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
        //Todo ???????????????????????????,???????????????????????????
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
     * ???????????????????????????????????????,??????????????????,???????????????
     * ?????????????????????,???????????????,????????????????????????????????????
     * ????????????jdk???????????????
     * ???????????????????????????
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
     * ??????redisson??????????????????
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatelogJsonWithRedissonLock() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            //???????????? ???????????? ????????????
            RLock lock = redissonClient.getLock("CatelogJson-lock");
            lock.lock();//??????
            System.out.println("????????????????????????");
            Map<String, List<Catelog2Vo>> catelogJsonFromDb;
            try {
                catelogJsonFromDb = getCatelogJsonFromDb();
                //??????????????????json??????????????????
                String jsonString = JSON.toJSONString(catelogJsonFromDb);
                redisTemplate.opsForValue().set("catalogJson", jsonString, 1, TimeUnit.DAYS);
            } finally {
                lock.unlock();//??????
            }
            return catelogJsonFromDb;
        }
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    /**
     * ????????????redis????????????
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatelogJsonWithRedisLock() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            String uuid = UUID.randomUUID().toString();
            Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
            if (lock == null || lock) {
                System.out.println("????????????????????????");
                Map<String, List<Catelog2Vo>> catelogJsonFromDb;
                try {
                    catelogJsonFromDb = getCatelogJsonFromDb();
                    //??????????????????json??????????????????
                    String jsonString = JSON.toJSONString(catelogJsonFromDb);
                    redisTemplate.opsForValue().set("catalogJson", jsonString, 1, TimeUnit.DAYS);
                } finally {
                    //?????????  lua????????????????????????
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
                System.out.println("????????????????????????,????????????...");
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
     * ???????????????,???????????????????????????
     *
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatelogJsonWithLocalLock() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            synchronized (this) {
                Map<String, List<Catelog2Vo>> catelogJsonFromDb = getCatelogJsonFromDb();
                //??????????????????json??????????????????
                String jsonString = JSON.toJSONString(catelogJsonFromDb);
                redisTemplate.opsForValue().set("catalogJson", jsonString, 1, TimeUnit.DAYS);
                return catelogJsonFromDb;
            }
        }
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    /**
     * ???????????????????????????????????????
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatelogJsonFromDb() {
        //????????????????????????????????????????????????????????????
//        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
//        if (!StringUtils.isEmpty(catalogJson)) {
//            System.out.println("????????????????????????,????????????....");
//            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
//            });
//        }
        //????????????????????????????????????
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
        //?????????????????????id
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
                    //???????????????
                    categoryEntity.setChildren(getChildren(categoryEntity, all));
                    return categoryEntity;
                })
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }

}