package com.freedy.mall.product.service.impl;

import com.freedy.mall.product.entity.SpuInfoEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.Query;

import com.freedy.mall.product.dao.SkuInfoDao;
import com.freedy.mall.product.entity.SkuInfoEntity;
import com.freedy.mall.product.service.SkuInfoService;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * key: a
     * catelogId: 225
     * brandId: 1
     * min: 0
     * max: 10000
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        String key =(String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and(w-> w.eq("sku_id",key).or().
                    like("sku_name",key).or().
                    like("sku_title",key)).or().
                    like("sku_subtitle",key);
        }
        String min =(String) params.get("min");
        if (!StringUtils.isEmpty(min)){
            wrapper.ge("price",min);
        }

        String max =(String) params.get("max");
        if (!StringUtils.isEmpty(max)&&Integer.parseInt(max)>0){
            wrapper.le("price",max);
        }

        String brandId =(String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId)&&Long.parseLong(brandId)>0){
            wrapper.eq("brand_id",brandId);
        }

        String catelogId =(String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId)&&Long.parseLong(catelogId)>0){
            wrapper.eq("catalog_id",catelogId);
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long supId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id",supId));
    }

}