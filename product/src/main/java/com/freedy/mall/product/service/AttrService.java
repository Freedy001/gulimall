package com.freedy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.product.entity.AttrEntity;
import com.freedy.mall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 15:44:51
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catId, String type);

    AttrVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelation(Long attrgroupId);
    /**
     * 获取当前分组没有关联的属性
     * @param params
     * @param attrgroupId
     * @return
     */
    PageUtils getNoRelation(Map<String, Object> params, Long attrgroupId);

    /**
     * 在指定的所有属性集合里面,挑出检索属性
     * @param attrId
     * @return
     */
    List<Long> selectSearchAttrIds(List<Long> attrId);
}

