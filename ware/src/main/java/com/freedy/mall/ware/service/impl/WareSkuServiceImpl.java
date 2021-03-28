package com.freedy.mall.ware.service.impl;

import com.freedy.common.to.mq.StockLockedTo;
import com.freedy.mall.ware.entity.WareOrderTaskDetailEntity;
import com.freedy.mall.ware.entity.WareOrderTaskEntity;
import com.freedy.mall.ware.exception.NoStockException;
import com.freedy.mall.ware.service.WareOrderTaskDetailService;
import com.freedy.mall.ware.service.WareOrderTaskService;
import com.freedy.mall.ware.vo.OrderItemVo;
import com.freedy.mall.ware.vo.SkuHasStockVo;
import com.freedy.mall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.Query;

import com.freedy.mall.ware.dao.WareSkuDao;
import com.freedy.mall.ware.entity.WareSkuEntity;
import com.freedy.mall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }


        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {

    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        return skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            vo.setSkuID(skuId);
            Integer stock = baseMapper.getStocksBySkuId(skuId);
            vo.setHasStock(stock != null && stock > 0);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     *Transactional 只要是运行时异常都会回滚\
     * 为某个订单锁定库存
     *
     * 库存解锁场景
     *  1.下订单成功，订单过期没有支付被系统自动取消、被用户手动取消。都要解锁库存
     *  2.下单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。
     *      之前锁定的库存就要自动解锁
     */
    @Transactional
    @Override
    public boolean orderLockStock(WareSkuLockVo vo) {
        WareOrderTaskEntity taskEntity=new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);
        //找到每个商品在哪个仓库都有库存
        List<OrderItemVo> voLocks = vo.getLocks();
        for (OrderItemVo item : voLocks) {
            Long skuId = item.getSkuId();
            List<Long> wareIds = baseMapper.listWareIdWhitchHasSkuStock(skuId);
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            boolean allLock = false;
            for (Long wareId : wareIds) {
                //成功返回1 否则就是0
                Long count = baseMapper.lockSkuStock(skuId, wareId, item.getCount());
                if (count == 1) {
                    allLock = true;
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(
                            null, skuId, "", item.getCount(),
                            taskEntity.getId(),wareId,1);
                    wareOrderTaskDetailService.save(entity);
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());
                    lockedTo.setDetailId(entity.getId());
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked", lockedTo);
                    break;
                }
            }
            if (!allLock) {
                throw new NoStockException(skuId);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean orderLockStockAuto(WareSkuLockVo vo) {
        //找到每个商品在哪个仓库都有库存
        for (OrderItemVo item : vo.getLocks()) {
            Long skuId = item.getSkuId();
            Integer count = baseMapper.lockSkuStockAuto(item.getSkuId(),item.getCount());
            if (count != 1) {
                throw new NoStockException(skuId);
            }
        }
        return true;
    }


    @Override
    public void releaseLockStock() {

    }

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message){
        Long id = to.getId();
        WareOrderTaskEntity byId = wareOrderTaskService.getById(id);
        if (byId!=null){
            //
        }
    }

}