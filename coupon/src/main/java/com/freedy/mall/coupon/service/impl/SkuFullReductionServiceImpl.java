package com.freedy.mall.coupon.service.impl;

import com.freedy.common.to.SkuReductionTo;
import com.freedy.mall.coupon.entity.MemberPriceEntity;
import com.freedy.mall.coupon.entity.SkuLadderEntity;
import com.freedy.mall.coupon.service.MemberPriceService;
import com.freedy.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.Query;

import com.freedy.mall.coupon.dao.SkuFullReductionDao;
import com.freedy.mall.coupon.entity.SkuFullReductionEntity;
import com.freedy.mall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存sku的优惠、满减等信息‘mall_sms -> sms_sku_ladder\sms_sku_full_reduction\sms_member_price\’
     * @param skuReductionTo
     */
    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuReductionTo,skuLadderEntity);
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if (skuReductionTo.getFullCount()>0){
            skuLadderService.save(skuLadderEntity);
        }

        //满减sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo,skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuReductionTo.getCountStatus());
        if (skuReductionTo.getFullPrice().compareTo(new BigDecimal(0))>0){
            this.save(skuFullReductionEntity);
        }
        //会员价格sms_member_price
        List<SkuReductionTo.MemberPriceBean> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntities = memberPrice.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            //todo
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(item-> item.getMemberPrice().compareTo(new BigDecimal(0)) > 0).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntities);
    }

}