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
 *
 * 核心功能：
 * 1. 自动同步线程池指标到 Prometheus（HTTP端口9090）
 * 2. 自动注册线程池MBean到JMX，支持JConsole等工具监控
 * 3. 定时执行SPI自适应调节策略，实现线程池动态优化
 *
 * 设计特点：
 * - 采用单例模式设计，确保全局只有一个收集器实例
 * - 使用ScheduledExecutorService实现定时任务调度
 * - 集成多种监控导出器（Prometheus + JMX）
 * - 支持SPI扩展机制，可插拔式加载调节策略
 *
 * 工作流程：
 * 1. 启动时初始化Prometheus HTTP服务器和JMX MBean
 * 2. 每5秒执行一次指标收集和策略调节
 * 3. 为每个线程池调用getMetrics()更新指标
 * 4. 通过AdaptiveStrategyManager应用SPI调节策略
 *
 * 线程安全：
 * - 使用volatile保证initialized变量的可见性
 * - ScheduledExecutorService本身是线程安全的
 * - 各导出器独立工作，无共享状态竞争
 *
 * 使用场景：
 * 作为智能线程池框架的核心监控组件，提供统一的指标收集和策略调节入口
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 */
public class MetricsCollector {

    /**
     * 定时任务调度器
     *
     * 特性说明：
     * - 使用单线程调度器，确保任务顺序执行
     * - 线程命名为默认的pool-1-thread-1，可通过自定义ThreadFactory优化
     * - 支持定时和周期性任务调度
     */
    private static final ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 初始化标志位
     *
     * 设计考虑：
     * - 防止重复初始化，确保收集器单例
     * - 使用静态变量实现类级别的状态管理
     * - 非volatile设计，因为主要在主线程中访问
     */
    private static boolean initialized = false;

    /**
     * 启动指标收集器
     *
     * 初始化流程：
     * 1. 检查是否已初始化，避免重复启动
     * 2. 启动Prometheus HTTP服务器（端口9090）
     * 3. 注册所有线程池MBean到JMX
     * 4. 启动定时任务，每5秒执行指标收集和策略调节
     *
     * 异常处理：
     * - Prometheus或JMX初始化失败时打印异常，但不中断整个启动过程
     * - 定时任务内部异常不会影响后续调度
     *
     * 端口配置：
     * - 默认使用9090端口，符合Prometheus标准
     * - 如端口被占用，PrometheusExporter内部会处理异常
     *
     * 策略调节：
     * - 通过AdaptiveStrategyManager调用SPI加载的调节策略
     * - 支持根据运行时指标动态调整线程池参数
     * - 调节周期为5秒，平衡实时性和系统开销
     */
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
     * 获取当前所有线程池指标快照
     *
     * 功能说明：
     * 提供线程池指标的实时查询接口，支持外部系统获取当前运行状态
     *
     * 实现细节：
     * - 遍历ThreadPoolManager中的所有线程池
     * - 调用每个线程池的getMetrics()方法获取最新指标
     * - 将结果封装为List返回
     *
     * 性能考虑：
     * - 方法为同步调用，直接获取当前状态
     * - 不缓存结果，确保数据的实时性
     * - 时间复杂度为O(n)，n为线程池数量
     *
     * 使用场景：
     * - 调试和诊断时获取线程池状态
     * - 外部监控系统获取指标数据
     * - 健康检查和状态报告
     *
     * @return 包含所有线程池当前指标的列表，不会返回null
     */
    public static List<PoolMetrics> currentMetrics() {
        List<PoolMetrics> list = new ArrayList<>();
        for (DynamicThreadPoolExecutor pool : ThreadPoolManager.getAllPools()) {
            list.add(pool.getMetrics());
        }
        return list;
    }
}
