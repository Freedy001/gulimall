package com.freedy.mall.search.thread;

import java.util.concurrent.*;

/**
 * @author Freedy
 * @date 2021/3/6 10:06
 */
public class ThreadTest {
    //当前系统中池只有一两个,每个异步任务,提交给线程池让他自己去执行
    public static ExecutorService service = Executors.newFixedThreadPool(10);
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main thread...id:"+Thread.currentThread().getId());
        /**
         *  1、集成thread
         *  Thread01 thread01 = new Thread01();
         *  thread01.start();
         *  2、实现runnable接口
         *  new Thread(new Runnable01()).start();
         *  3、实现Callable接口+FutureTask(可以拿到返回结果,可以处理异常)
         *  FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
         *  new Thread(futureTask).start();
         *  //阻塞 等待整个线程执行完成 获取返回结果
         *  Integer integer = futureTask.get();
         *  4、线程池[ExecutorService]
         *  给线程池直接提交任务
         *  service.execute(new Runnable01());
         *      1、创建
         *          1)、Executors
         *          2）、new ThreadPoolExecutor()
         *  区别：
         *   1、2不能获取返回值。3可以获取返回值
         *   1、2、3都不能控制资源
         *   4、可以控制资源 性能稳定
         */
        /**
         *  创建线程池的七大参数
         *  int corePoolSize, 核心线程数[一直存在]：线程池创建好后就准备就绪的线程数量，
         *                    就等待来接受异步任务去执行
         *  int maximumPoolSize, 最大线程数量
         *  long keepAliveTime, 存活时间：如果当前线程数量大于核心数量。
         *                      释放线程，在指定的keepAliveTime时间后
         *  TimeUnit unit, 时间单位
         *  BlockingQueue<Runnable> workQueue, :阻塞队列 若干任务有很多，就会将多的任务放在队列里面，
         *                                      只要有线程空闲，就回去队列里面取出新的任务去执行
         *  ThreadFactory threadFactory, 线程创建工厂
         *  RejectedExecutionHandler handler 处理workQueue溢出的任务
         *  ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
         *                 5,
         *                 200,
         *                 10,
         *                 TimeUnit.SECONDS,
         *                 new LinkedBlockingQueue<>(100000),
         *                 Executors.defaultThreadFactory(),
         *                 new ThreadPoolExecutor.AbortPolicy()
         * );
         */
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果:" + i);
//        }, service);
        CompletableFuture<String> fImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品图片信息信息----------" + Thread.currentThread().getId());
            return "hello.jpg";
        },service);
        CompletableFuture<String> fAttr = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("查询商品的属性----------" + Thread.currentThread().getId());
            return "black";
        },service);
        CompletableFuture<String> fTrans = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询物流信息----------" + Thread.currentThread().getId());
            return "已到达";
        },service);
        CompletableFuture<Void> allOf = CompletableFuture.allOf(fImg, fAttr, fTrans);
        allOf.get();
        System.out.println("main end......");
    }

    public static class Callable01 implements Callable<Integer>{
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程"+Thread.currentThread().getId());
            int i=10/2;
            System.out.println("运行结果:"+i);
            return i;
        }
    }

    public static class Thread01 extends Thread{
        @Override
        public void run() {
            System.out.println("当前线程"+Thread.currentThread().getId());
            int i=10/2;
            System.out.println("运行结果:"+i);
        }
    }

    public static class Runnable01 implements Runnable{
        @Override
        public void run() {
            System.out.println("当前线程"+Thread.currentThread().getId());
            int i=10/2;
            System.out.println("运行结果:"+i);
        }
    }
}
