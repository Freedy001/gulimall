package com.freedy.mall.product.service.impl;

import com.freedy.mall.product.service.CategoryBrandRelationService;
import com.freedy.mall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

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
                .map((menu)->{
                    menu.setChildren(getChildren(menu,entities));
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
        List<Long> path =new ArrayList<>();
        findParentPath(catelogId,path);
        Collections.reverse(path);
        return path.toArray(new Long[path.size()]);
    }

    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())){
            System.out.println(category.getCatId()+category.getName());
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
        }
    }

    @Override
    public List<CategoryEntity> getLevel1Categories() {
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        /**
         * 将数据的多次查询变为一次
         */
        List<CategoryEntity> allEntity = baseMapper.selectList(null);
        List<CategoryEntity> level1Categories = getLevel1Categories();
        return level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> entities = getNextLevelByCatId(allEntity,v.getCatId());
            List<Catelog2Vo> Catelog2Vo = new ArrayList<>();
            if (entities != null) {
                Catelog2Vo = entities.stream().map(item -> {
                    List<CategoryEntity> entitiesLevel3 = getNextLevelByCatId(allEntity,item.getCatId());
                    List<Catelog2Vo.Catelog3Vo> collect = entitiesLevel3.stream().map(i -> new Catelog2Vo.Catelog3Vo(item.getCatId().toString(), i.getCatId().toString(), i.getName())).collect(Collectors.toList());
                    return new Catelog2Vo(v.getCatId().toString(), item.getCatId().toString(), item.getName(), collect);
                }).collect(Collectors.toList());
            }
            return Catelog2Vo;
        }));
    }

    private List<CategoryEntity> getNextLevelByCatId(List<CategoryEntity> allEntity,Long catId) {
        return allEntity.stream().filter(item -> Objects.equals(item.getParentCid(), catId)).collect(Collectors.toList());
    }

    private void findParentPath(Long catelogId, List<Long> path){
        //收集当前节点的id
        path.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),path);
        }
    }

    private List<CategoryEntity> getChildren(CategoryEntity root,List<CategoryEntity> all){
        return all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .map(categoryEntity -> {
                    //找到子菜单
                    categoryEntity.setChildren(getChildren(categoryEntity,all));
                    return categoryEntity;
                })
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
    }

}