package com.smart.pool.monitor;

import com.smart.pool.core.DynamicThreadPoolExecutor;
import com.smart.pool.core.ThreadPoolManager;
import com.smart.pool.core.metrics.PoolMetrics;
import com.smart.pool.core.strategy.AdaptiveStrategyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 统一线程池指标收集器
 * - 自动同步到 Prometheus
 * - 自动注册到 JMX
 * - 自动应用 SPI 调整策略
 */
public class MetricsCollector {

    private static final ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
    private static boolean initialized = false;

    public static void startCollecting() {
        if (initialized) return;
        initialized = true;

        try {
            // 启动 Prometheus HTTPServer
            PrometheusExporter exporter = new PrometheusExporter();
            exporter.start(9090);

            // 注册 JMX MBean
            JmxMetricsExporter.export();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 定时刷新指标并执行 SPI 策略
        monitor.scheduleAtFixedRate(() -> {
            ThreadPoolManager.getAllPools().forEach(pool -> {
                // 更新指标
                pool.getMetrics();

                // 调用 SPI 调节策略
                AdaptiveStrategyManager.adjust(pool);
            });
        }, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * 获取当前所有线程池指标
     */
    public static List<PoolMetrics> currentMetrics() {
        List<PoolMetrics> list = new ArrayList<>();
        for (DynamicThreadPoolExecutor pool : ThreadPoolManager.getAllPools()) {
            list.add(pool.getMetrics());
        }
        return list;
    }
}
