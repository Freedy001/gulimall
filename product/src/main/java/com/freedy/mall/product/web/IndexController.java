package com.freedy.mall.product.web;

import com.freedy.mall.product.entity.CategoryEntity;
import com.freedy.mall.product.service.CategoryService;
import com.freedy.mall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Freedy
 * @date 2021/2/22 23:56
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/index.html", "/"})
    public String indexPage(Model model) {
        List<CategoryEntity> entities = categoryService.getLevel1Categories();
        model.addAttribute("categories", entities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson(){
        return categoryService.getCatelogJson();
    }

    @ResponseBody
    @GetMapping("/product/hello")
    public String hello(){
        //获取一把锁,只要锁名一样,就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");
        //加锁
        lock.lock(10, TimeUnit.SECONDS);
        try {
            System.out.println("加锁成功"+Thread.currentThread().getId());
            try {
                Thread.sleep(3000) ;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            System.out.println("释放锁"+Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    @ResponseBody
    @GetMapping("/write")
    public String write(){
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock =lock.writeLock();
        String s="";
        rLock.lock();
        try {
            s = UUID.randomUUID().toString();
            Thread.sleep(3000);
            redisTemplate.opsForValue().set("val",s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }
        return s;
    }

    @ResponseBody
    @GetMapping("/read")
    public String read(){
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = lock.readLock();
        String s= "";
        rLock.lock();
        try{
            s = redisTemplate.opsForValue().get("val");
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }
        return s;
    }

    @GetMapping("/park")
    @ResponseBody
    public String park(){
        RSemaphore park = redissonClient.getSemaphore("park");
        try {
            park.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "ok";
    }

    @GetMapping("/go")
    @ResponseBody
    public String go(){
        RSemaphore park = redissonClient.getSemaphore("park");
        try {
            park.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }

    @ResponseBody
    @GetMapping("/lockoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();//等待闭锁完成
        return "放假了";
    }

    @ResponseBody
    @GetMapping("/gogogo/{id}")
    public String gogogo(@PathVariable Long id){
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();
        return id+"班的人都走了";
    }

}
