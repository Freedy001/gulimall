package com.freedy.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.freedy.common.to.mq.OrderTo;
import com.freedy.common.to.mq.StockLockedTo;
import com.freedy.common.utils.R;
import com.freedy.mall.ware.entity.WareOrderTaskDetailEntity;
import com.freedy.mall.ware.entity.WareOrderTaskEntity;
import com.freedy.mall.ware.exception.NoStockException;
import com.freedy.mall.ware.feign.OrderFeignService;
import com.freedy.mall.ware.service.WareOrderTaskDetailService;
import com.freedy.mall.ware.service.WareOrderTaskService;
import com.freedy.mall.ware.vo.OrderEntity;
import com.freedy.mall.ware.vo.OrderItemVo;
import com.freedy.mall.ware.vo.SkuHasStockVo;
import com.freedy.mall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    @Autowired
    OrderFeignService orderFeignService;

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
     * Transactional ????????????????????????????????????\
     */
    @Transactional
    @Override
    public boolean orderLockStock(WareSkuLockVo vo) {
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);
        //?????????????????????????????????????????????
        List<OrderItemVo> voLocks = vo.getLocks();
        for (OrderItemVo item : voLocks) {
            Long skuId = item.getSkuId();
            List<Long> wareIds = baseMapper.listWareIdWhitchHasSkuStock(skuId);
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            boolean allLock = false;
            for (Long wareId : wareIds) {
                //????????????1 ????????????0
                Long count = baseMapper.lockSkuStock(skuId, wareId, item.getCount());
                if (count == 1) {
                    allLock = true;
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(
                            null, skuId, "", item.getCount(),
                            taskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(entity);
                    break;
                }
            }
            if (!allLock) {
                throw new NoStockException(skuId);
            }
        }
        StockLockedTo lockedTo = new StockLockedTo();
        lockedTo.setId(taskEntity.getId());
        rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
        return true;
    }

    @Override
    @Transactional
    public boolean orderLockStockAuto(WareSkuLockVo vo) {
        //?????????????????????????????????????????????
        for (OrderItemVo item : vo.getLocks()) {
            Long skuId = item.getSkuId();
            Integer count = baseMapper.lockSkuStockAuto(item.getSkuId(), item.getCount());
            if (count != 1) {
                throw new NoStockException(skuId);
            }
        }
        return true;
    }

    @Transactional
    @Override
    public void unLockStock(StockLockedTo to, Message message, Channel channel) throws IOException {
        Long taskId = to.getId();
        WareOrderTaskEntity byId = wareOrderTaskService.getById(taskId);
        if (byId != null) {
            //??????????????????
            R orderStatus = orderFeignService.getOrderStatus(byId.getOrderSn());
            OrderEntity data = orderStatus.getData(new TypeReference<OrderEntity>() {
            });
            //??????????????????????????????????????????????????????
            if (data == null || data.getStatus() == 4) {
                List<WareOrderTaskDetailEntity> taskDetailList =
                        wareOrderTaskDetailService.list(
                                new QueryWrapper<WareOrderTaskDetailEntity>()
                                        .eq("task_id", taskId));
                for (WareOrderTaskDetailEntity detailEntity : taskDetailList) {
                    //???Status=1?????????????????????
                    if (detailEntity.getLockStatus()==1){
                        System.out.println("??????"+byId.getOrderSn()+"?????? ??????"+detailEntity.getSkuId()+"???????????????");
                        unLockStock(
                                detailEntity.getSkuId(),
                                detailEntity.getWareId(),
                                detailEntity.getSkuNum()
                        );
                        wareOrderTaskDetailService.updateLockStatus(detailEntity.getId());
                    }
                }
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }else if (data.getStatus()==0){
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }else{
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        }else {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     */
    @Transactional
    @Override
    public void unLockStock(OrderTo orderTo, Message message, Channel channel) throws IOException {
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getOne(
                new QueryWrapper<WareOrderTaskEntity>()
                        .eq("order_sn", orderTo.getOrderSn())
        );
        List<WareOrderTaskDetailEntity> detail = wareOrderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", taskEntity.getId())
        );
        for (WareOrderTaskDetailEntity entity : detail) {
            //???Status=1?????????????????????
            if (entity.getLockStatus()==1){
                System.out.println("??????"+orderTo.getOrderSn()+"?????? ??????"+entity.getSkuId()+"???????????????");
                unLockStock(
                        entity.getSkuId(),
                        entity.getWareId(),
                        entity.getSkuNum()
                );
                wareOrderTaskDetailService.updateLockStatus(entity.getId());
            }
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }


    @Override
    public void unLockStock(Long skuId, Long wareId, Integer count){
        baseMapper.unLockStock(skuId,wareId,count);
    }



}