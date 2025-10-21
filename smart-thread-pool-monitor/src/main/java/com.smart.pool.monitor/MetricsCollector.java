package com.smart.pool.monitor;

import com.smart.pool.core.DynamicThreadPoolExecutor;
import com.smart.pool.core.ThreadPoolManager;
import com.smart.pool.core.metrics.PoolMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 统一线程池指标收集器
 * - 自动同步到 Prometheus
 * - 自动注册到 JMX
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

        // 定时刷新指标（Prometheus 和 JMX 都会读取 DynamicThreadPoolExecutor.getMetrics()）
        monitor.scheduleAtFixedRate(() -> {
            ThreadPoolManager.getAllPools().forEach(DynamicThreadPoolExecutor::getMetrics);
        }, 0, 5, TimeUnit.SECONDS);
    }

    public static List<PoolMetrics> currentMetrics() {
        List<PoolMetrics> list = new ArrayList<>();
        for (DynamicThreadPoolExecutor pool : ThreadPoolManager.getAllPools()) {
            list.add(pool.getMetrics());
        }
        return list;
    }
}
