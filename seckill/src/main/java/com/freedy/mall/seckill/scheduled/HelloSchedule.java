package com.freedy.mall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Freedy
 * @date 2021/3/31 11:26
 *
 * 开启定时任务
 */
@Slf4j
@Component
public class HelloSchedule {

    /**
     * 在spring中必须为6位
     * 在周几的位置1-7表示周一到周日
     * 定时任务默认是阻塞的
     *      可以让业务运行以异步的方式，自己提交给线程池
     *@Async 可以让定时任务异步执行
     */
    @Async
    //@Scheduled(cron = "* * * * * ?")
    public void hello() throws InterruptedException {
        log.info("hello......");
        Thread.sleep(3000);
    }

}
