package com.freedy.mall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.freedy.common.to.SeckillSkuRedisTo;
import com.freedy.common.utils.R;
import com.freedy.mall.product.entity.SkuImagesEntity;
import com.freedy.mall.product.entity.SpuInfoDescEntity;
import com.freedy.mall.product.entity.SpuInfoEntity;
import com.freedy.mall.product.feign.SecKillFeignService;
import com.freedy.mall.product.service.*;
import com.freedy.mall.product.vo.SkuItemSaleAttrVo;
import com.freedy.mall.product.vo.SkuItemVo;
import com.freedy.mall.product.vo.SpuItemAttrGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.Query;

import com.freedy.mall.product.dao.SkuInfoDao;
import com.freedy.mall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService imagesService;

    @Autowired
    SpuInfoDescService descService;

    @Autowired
    AttrGroupService groupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor poolExecutor;

    @Autowired
    SecKillFeignService secKillFeignService;

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

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();
        //sku?????????????????????
        CompletableFuture<Void> future1 = CompletableFuture.supplyAsync(() ->
                getById(skuId), poolExecutor).thenAcceptAsync((val) ->
        {
            skuItemVo.setInfo(val);
            Long spuId = val.getSpuId();
            Long catalogId = val.getCatalogId();
            //??????spu?????????????????????
            CompletableFuture<Void> future3 = CompletableFuture.supplyAsync(() ->
                            skuSaleAttrValueService.getSaleAttrsBySpuId(spuId)
                    , poolExecutor).thenAccept(skuItemVo::setSaleAttr);
            //??????spu?????????
            CompletableFuture<Void> future4 = CompletableFuture.supplyAsync(() ->
                            descService.getById(spuId)
                    , poolExecutor).thenAccept(skuItemVo::setDesc);
            //??????spu?????????????????????
            CompletableFuture<Void> future5 = CompletableFuture.supplyAsync(() ->
                            groupService.getAttrGroupWithAttrsBySpuIdAndCatalogId(spuId, catalogId)
                    , poolExecutor).thenAccept(skuItemVo::setGroupAttrs);
            try {
                CompletableFuture.allOf(future3,future4,future5).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        //sku???????????????
        CompletableFuture<Void> future2 = CompletableFuture.supplyAsync(() -> imagesService.getImageBySkuId(skuId)
                , poolExecutor).thenAccept(skuItemVo::setImages);
        //????????????sku??????????????????
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
            R info = secKillFeignService.getSkuSecKillInfo(skuId);
            List<SeckillSkuRedisTo> data = info.getData(new TypeReference<List<SeckillSkuRedisTo>>() {
            });
            Long minTime = Long.MAX_VALUE;
            SeckillSkuRedisTo min = null;
            for (SeckillSkuRedisTo datum : data) {
                if (datum.getStartTime() < minTime) {
                    minTime = datum.getStartTime();
                    min = datum;
                }
            }
            skuItemVo.setSeckillSkuInfo(min);
        },poolExecutor);
        //???????????????????????????
        CompletableFuture.allOf(future1,future2,future3).get();
        return skuItemVo;
    }


}