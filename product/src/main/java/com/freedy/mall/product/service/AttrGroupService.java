package com.freedy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.product.entity.AttrGroupEntity;
import com.freedy.mall.product.vo.AttrGroupRelationVo;
import com.freedy.mall.product.vo.SkuItemVo;
import com.freedy.mall.product.vo.SpuItemAttrGroup;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 15:44:51
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    void deleteRelation(AttrGroupRelationVo[] vos);

    List<AttrGroupEntity> getAttrGroupById(Long catId);

    /**
     * 查出当前spu对应的所有属性分组的信息
     * 以及当前分组下的所有属性对应的的值
     * @param spuId
     * @return
     */
    List<SpuItemAttrGroup> getAttrGroupWithAttrsBySpuIdAndCatalogId(Long spuId, Long catalogId);

}

