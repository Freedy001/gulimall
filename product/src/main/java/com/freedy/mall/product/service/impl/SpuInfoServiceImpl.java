package com.freedy.mall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.freedy.common.constant.ProductConstant;
import com.freedy.common.to.SkuReductionTo;
import com.freedy.common.to.SpuBoundTo;
import com.freedy.common.to.es.SkuEsModel;
import com.freedy.common.utils.R;
import com.freedy.mall.product.entity.*;
import com.freedy.mall.product.feign.CouponFeignService;
import com.freedy.mall.product.feign.SearchFeignService;
import com.freedy.mall.product.feign.WareFeignService;
import com.freedy.mall.product.service.*;
import com.freedy.mall.product.vo.SkuHasStockVo;
import com.freedy.mall.product.vo.SpuSaveVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.Query;

import com.freedy.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Transactional
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1.保存spu基本信息‘pms_spu_info’
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        saveBaseSpuInfo(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();

        //2。保存spu的描述图片‘pms_spu_info_desc’
        List<String> describe = vo.getDecript();
        SpuInfoDescEntity describeEntity = new SpuInfoDescEntity();
        describeEntity.setSpuId(spuId);
        describeEntity.setDecript(String.join(",", describe));
        spuInfoDescService.saveSpuInfoDesc(describeEntity);

        //3.保存spu的图片集‘pms_spu_images’
        List<String> images = vo.getImages();
        spuImagesService.saveImages(images, spuId);

        //4.保存spu规格参数‘pms_product_attr_value’
        List<SpuSaveVo.BaseAttrsBean> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(item.getAttrId());
            AttrEntity attrEntity = attrService.getById(item.getAttrId());
            valueEntity.setAttrName(attrEntity.getAttrName());
            valueEntity.setAttrValue(item.getAttrValues());
            valueEntity.setQuickShow(item.getShowDesc());
            valueEntity.setSpuId(spuId);
            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities);
        //5.保存spu积分信息‘mall_sms ->sms_spu_bounds’
        SpuSaveVo.BoundsBean bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuId);
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败！");
        }

        //6.保存sku的基本信息‘pms_sku_info’
        List<SpuSaveVo.SkusBean> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                //        private String skuName;
                //        private BigDecimal price;
                //        private String skuTitle;
                //        private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(vo.getBrandId());
                skuInfoEntity.setCatalogId(vo.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuId);
                String defaultImg = "";
                for (SpuSaveVo.SkusBean.ImagesBean img : item.getImages()) {
                    if (img.getDefaultImg() == 1) {
                        defaultImg = img.getImgUrl();
                        break;
                    }
                }
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                List<String> descar = item.getDescar();
                skuInfoEntity.setSkuDesc(String.join("-", descar));
                skuInfoService.save(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();

                //7.保存sku图片信息‘pms_sku_images’
                List<SkuImagesEntity> skuImagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    BeanUtils.copyProperties(img, skuImagesEntity);
                    skuImagesEntity.setSkuId(skuId);
                    return skuImagesEntity;
                }).filter(entity -> !StringUtils.isEmpty(entity.getImgUrl())).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                //8.保存sku的销售属性信息‘pms_sku_sale_attr_value’
                List<SpuSaveVo.SkusBean.AttrBean> attrList = item.getAttr();
                List<SkuSaleAttrValueEntity> skuAttr = attrList.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuAttr);


                //9.保存sku的优惠、满减等信息‘mall_sms -> sms_sku_ladder\sms_sku_full_reduction\sms_member_price\’
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) > 0) {
                    R r1 = couponFeignService.savaSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败！");
                    }
                }
            });
        }

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    /**
     * status: 0
     * key: a
     * brandId: 1
     * catelogId: 225
     * page: 1
     * limit: 10
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> w.eq("id", key).or().like("spu_name", key).or().like("spu_description", key));
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    /**
     * 商品上架
     *
     * @param spuId
     */
    @Override
    public void up(Long spuId) throws Exception {
        //组装需要的数据
        List<ProductAttrValueEntity> spuAttrs = productAttrValueService.getAttrBySpuId(spuId);
        List<Long> attrId = spuAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long>resultId=attrService.selectSearchAttrIds(attrId);
        Set<Long> idSet =new HashSet<>(resultId);
        List<SkuEsModel.Attr> attrs1 = spuAttrs.stream().map(item -> {
            SkuEsModel.Attr attr = new SkuEsModel.Attr();
            BeanUtils.copyProperties(item, attr);
            return attr;
        }).filter(item-> idSet.contains(item.getAttrId())).collect(Collectors.toList());

        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        Map<Long, Boolean> hasStockMap = null;
        try {
            R hasStock = wareFeignService.getSkusHasStock(skuIds);
            hasStockMap = hasStock.getData(new TypeReference<List<SkuHasStockVo>>(){}).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuID, SkuHasStockVo::isHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常:{}",e);
        }
        Map<Long, Boolean> finalHasStockMap = hasStockMap;
        List<SkuEsModel> upProduct = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            //todo 发送远程调用 查询是否有库存
            //todo 热度评分
            if (finalHasStockMap==null){
                skuEsModel.setHasStock(true);
            }else {
                skuEsModel.setHasStock(finalHasStockMap.get(sku.getSkuId()));
            }
            skuEsModel.setHotScore(0L);
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());
            //设置属性
            skuEsModel.setAttrs(attrs1);
            return skuEsModel;
        }).collect(Collectors.toList());
        R r = searchFeignService.productStatusUp(upProduct);
        if (r.getCode()==0){
            //修改当前spu的状态
            baseMapper.updateSpuPublishStatus(spuId, ProductConstant.Status.SPU_UP.getCode());
        }else {
            log.error("远程调用searchFeignService失败");
            //todo 重复调用？接口幂等性
            throw new Exception("远程调用失败");
        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        return baseMapper.getSpuInfoBySkuId(skuId);
    }

}