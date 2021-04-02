package com.freedy.mall.coupon.service.impl;

import com.freedy.mall.coupon.entity.SeckillSkuRelationEntity;
import com.freedy.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.Query;

import com.freedy.mall.coupon.dao.SeckillSessionDao;
import com.freedy.mall.coupon.entity.SeckillSessionEntity;
import com.freedy.mall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {
        List<SeckillSessionEntity> list = baseMapper.selectList(
                new QueryWrapper<SeckillSessionEntity>()
                        .between("start_time", new Date()
                                , new Date(new Date().getTime() + 1000 * 60 * 60 * 24 * 3))
                .or().between("end_time", new Date()
                        , new Date(new Date().getTime() + 1000 * 60 * 60 * 24 * 3))
        );
        List<SeckillSessionEntity> collect=null;
        if (list!=null){
            collect = list.stream().peek(item -> item.setRelationSkus(
                    seckillSkuRelationService.list(
                    new QueryWrapper<SeckillSkuRelationEntity>()
                            .eq("promotion_session_id", item.getId())
                    )
                )
            ).collect(Collectors.toList());
        }
        return collect;
    }

}