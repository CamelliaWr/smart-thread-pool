package com.smart.pool.demo;

import com.smart.pool.core.DynamicThreadPoolExecutor;
import com.smart.pool.starter.annotation.SmartPool;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OrderService {

    @SmartPool(name = "order-pool", corePoolSize = 4, maxPoolSize = 8)
    private DynamicThreadPoolExecutor orderExecutor;

    public void processOrder(Runnable task) {
        orderExecutor.submit(task);
    }

    @PostConstruct
    public void init() {
        // 模拟持续产生订单请求
        Thread generator = new Thread(() -> {
            while (true) {
                processOrder(() -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(500 + (int)(Math.random() * 1500));
                    } catch (InterruptedException ignored) {}
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
        });
        generator.setDaemon(true);
        generator.start();
    }
}
