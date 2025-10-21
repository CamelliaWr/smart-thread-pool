package com.smart.pool.demo;

import com.smart.pool.core.DynamicThreadPoolExecutor;
import com.smart.pool.starter.annotation.SmartPool;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 订单服务类
 * <p>
 * 演示如何使用智能线程池处理订单相关业务。通过@SmartPool注解自动注入配置好的线程池，
 * 并使用该线程池异步处理订单请求。同时包含一个模拟订单生成器，用于持续产生订单请求进行测试。
 * </p>
 *
 * @author Smart Thread Pool Demo
 * @since 1.0.0
 */
@Service
public class OrderService {

    /**
     * 订单处理线程池
     * <p>
     * 通过@SmartPool注解自动注入，线程池名称为"order-pool"，
     * 核心线程数4个，最大线程数8个，用于处理订单相关的异步任务
     * </p>
     */
    @SmartPool(name = "order-pool", corePoolSize = 4, maxPoolSize = 8)
    private DynamicThreadPoolExecutor orderExecutor;

    /**
     * 处理订单任务
     * <p>
     * 将订单处理任务提交到线程池中异步执行，提高系统响应速度
     * </p>
     *
     * @param task 订单处理任务，不能为null
     * @throws NullPointerException 如果task参数为null
     */
    public void processOrder(Runnable task) {
        orderExecutor.submit(task);
    }

    /**
     * 初始化方法
     * <p>
     * 在Spring容器初始化完成后自动调用，启动一个后台线程模拟持续产生订单请求，
     * 用于演示和测试线程池的工作情况。该线程为守护线程，不会阻止应用正常关闭。
     * </p>
     */
    @PostConstruct
    public void init() {
        // 模拟持续产生订单请求
        Thread generator = new Thread(() -> {
            while (true) {
                // 提交订单处理任务，模拟订单处理耗时500-2000毫秒
                processOrder(() -> {
                    try {
                        // 随机睡眠500-2000毫秒，模拟订单处理时间
                        TimeUnit.MILLISECONDS.sleep(500 + (int)(Math.random() * 1500));
                    } catch (InterruptedException ignored) {
                        // 中断异常，忽略并继续
                    }
                });

                try {
                    // 每100毫秒产生一个新订单，模拟高并发场景
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                    // 中断异常，忽略并继续
                }
            }
        });

        // 设置为守护线程，不会阻止应用关闭
        generator.setDaemon(true);
        generator.start();
    }
}