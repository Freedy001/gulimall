package com.freedy.mall.product.service.impl;

import com.freedy.mall.product.dao.AttrAttrgroupRelationDao;
import com.freedy.mall.product.dao.CategoryDao;
import com.freedy.mall.product.entity.AttrAttrgroupRelationEntity;
import com.freedy.mall.product.entity.CategoryEntity;
import com.freedy.mall.product.vo.AttrGroupRelationVo;
import com.freedy.mall.product.vo.AttrGroupRespVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.Query;

import com.freedy.mall.product.dao.AttrGroupDao;
import com.freedy.mall.product.entity.AttrGroupEntity;
import com.freedy.mall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String)params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        if (catelogId!=0){
            wrapper.eq("catelog_id",catelogId);
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);
        //查询出分类名称
        List<AttrGroupRespVo> collect = page.getRecords().stream().map(item -> {
            AttrGroupRespVo attrGroupRespVo = new AttrGroupRespVo();
            BeanUtils.copyProperties(item, attrGroupRespVo);
            CategoryEntity categoryEntity = categoryDao.selectById(attrGroupRespVo.getCatelogId());
            if (categoryEntity!=null){
                attrGroupRespVo.setCateName(categoryEntity.getName());
            }
            return attrGroupRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(collect);
        return pageUtils;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        List<AttrAttrgroupRelationEntity> list = Arrays.asList(vos).stream().map(item -> {
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, entity);
            return entity;
        }).collect(Collectors.toList());
        relationDao.deleteBatcheRelation(list);
    }

    @Override
    public List<AttrGroupEntity> getAttrGroupById(Long catId) {
        return this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId));
    }

}