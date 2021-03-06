package com.freedy.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.freedy.common.constant.ProductConstant;
import com.freedy.mall.product.dao.AttrAttrgroupRelationDao;
import com.freedy.mall.product.dao.AttrGroupDao;
import com.freedy.mall.product.dao.CategoryDao;
import com.freedy.mall.product.entity.AttrAttrgroupRelationEntity;
import com.freedy.mall.product.entity.AttrGroupEntity;
import com.freedy.mall.product.entity.CategoryEntity;
import com.freedy.mall.product.service.CategoryService;
import com.freedy.mall.product.vo.AttrRespVo;
import com.freedy.mall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.Query;

import com.freedy.mall.product.dao.AttrDao;
import com.freedy.mall.product.entity.AttrEntity;
import com.freedy.mall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Transactional
@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @CacheEvict(cacheNames = {"attr"},allEntries = true)
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        //??????????????????
        this.save(attrEntity);
        //??????????????????
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            if (attr.getAttrGroupId() != null) {
                AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                relationEntity.setAttrId(attrEntity.getAttrId());
                relationEntity.setAttrGroupId(attr.getAttrGroupId());
                relationDao.insert(relationEntity);
            }
        }
    }


    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catId, String type) {
        //????????????+??????????????????+????????????
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attr_type", "base".equalsIgnoreCase(type) ?
                ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() :
                ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catId != 0) {
            queryWrapper.eq("catelog_id", catId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> wrapper.eq("attr_id", key).or().like("attr_name", key));
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        //????????????????????????
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> respVoList = records.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            //???????????????????????????
            if ("base".equalsIgnoreCase(type)) {
                AttrAttrgroupRelationEntity attrId = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrRespVo.getAttrId()));
                if (attrId != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrId.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            //???????????????????????????
            CategoryEntity categoryEntity = categoryDao.selectById(attrRespVo.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(respVoList);
        return pageUtils;
    }

    @Cacheable(cacheNames={"attr"},key = "#root.method.name+#root.args[0]",sync = true)
    @Override
    public AttrVo getAttrInfo(Long attrId) {
        AttrRespVo respVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, respVo);
        if (respVo.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //??????????????????
            AttrAttrgroupRelationEntity attrGroupRelation = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (attrGroupRelation != null) {
                respVo.setAttrGroupId(attrGroupRelation.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupRelation.getAttrGroupId());
                if (attrGroupEntity != null && attrGroupEntity.getAttrGroupName() != null) {
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        //??????????????????
        Long[] cateLogPath = categoryService.findCateLogPath(respVo.getCatelogId());
        respVo.setCatelogPath(cateLogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(respVo.getCatelogId());
        if (categoryEntity != null) {
            respVo.setCatelogName(categoryEntity.getName());
        }
        return respVo;
    }

    @CacheEvict(cacheNames = {"attr"},allEntries = true)
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()&&attr.getAttrGroupId()!=null) {
            //??????????????????
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());
            Integer count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count > 0) {
                relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            } else {
                relationDao.insert(relationEntity);
            }
        }
    }

    /**
     * ????????????id???????????????????????????
     *
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelation(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> e = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> collect = e.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        List<AttrEntity> entities = null;
        if (collect.size() > 0) {
            entities = this.listByIds(collect);
        }
        return entities;
    }

    @Override
    public PageUtils getNoRelation(Map<String, Object> params, Long attrgroupId) {
        //??????????????????????????????????????????????????????????????????
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //?????????????????????????????????????????????????????????
        List<AttrGroupEntity> list = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId)/*.ne("attr_group_id", attrgroupId)*/);
        List<AttrAttrgroupRelationEntity> relationEntities = null;
        if (list.size() > 0) {
            List<Long> collect = list.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
            relationEntities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        }
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (relationEntities != null && relationEntities.size() > 0) {
            List<Long> attrRelationList = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            //????????????????????????????????????????????????????????????
            wrapper.notIn("attr_id", attrRelationList);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrId) {
        return this.baseMapper.selectSearchAttrIds(attrId);
    }

}