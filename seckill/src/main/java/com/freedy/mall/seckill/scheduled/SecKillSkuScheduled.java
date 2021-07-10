package com.freedy.mall.seckill.scheduled;

import com.freedy.common.utils.R;
import com.freedy.mall.seckill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author Freedy
 * @date 2021/3/31 11:46
 * 秒杀商品的定时上架
 *  每天晚上3点，上架最近三天需要秒杀的商品
 *  当前
 */
@Slf4j
@RestController
public class SecKillSkuScheduled {

    @Autowired
    SecKillService secKillService;

    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock="seckill:upload:lock";

    @Scheduled(cron = "0 * * * * ?")
    @GetMapping("/upload")
    public R uploadSecKillSkuLatest3Days(){
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        //分布式锁 锁的业务执行完成，状态已经跟新完成
        //释放锁以后 其他人获取到就会拿到最新的状态
        try {
            secKillService.uploadSecKillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
        return R.ok();
    }


}
