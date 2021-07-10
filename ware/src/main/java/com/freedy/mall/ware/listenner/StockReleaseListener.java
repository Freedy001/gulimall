package com.freedy.mall.ware.listenner;

import com.freedy.common.to.mq.OrderTo;
import com.freedy.common.to.mq.StockLockedTo;
import com.freedy.mall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Freedy
 * @date 2021/3/28 22:32
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    /**
     * 为某个订单锁定库存
     *
     * 库存解锁场景
     * 1.下订单成功，订单过期没有支付被系统自动取消、被用户手动取消。都要解锁库存
     * 2.下单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。
     * 之前锁定的库存就要自动解锁
     *
     * 只要解锁库存失败。一定要告诉服务器此次解锁失败
     * 解锁成功就要跟新库存工作单的锁定状态
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        try {
            System.out.println("handleStockLockedRelease......");
            wareSkuService.unLockStock(to, message, channel);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            e.printStackTrace();
        }
    }

    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        try {
            System.out.println("handleOrderCloseRelease......");
            wareSkuService.unLockStock(orderTo, message, channel);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            e.printStackTrace();
        }
    }


}
