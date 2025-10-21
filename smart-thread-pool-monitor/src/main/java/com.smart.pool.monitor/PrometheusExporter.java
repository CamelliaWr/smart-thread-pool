package com.smart.pool.monitor;

import com.smart.pool.core.ThreadPoolManager;
import com.smart.pool.core.metrics.PoolMetrics;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;

/**
 * Prometheus指标导出器
 *
 * 功能说明：
 * 负责将智能线程池的运行指标以Prometheus格式暴露，支持通过HTTP端点进行监控数据采集
 *
 * 设计特点：
 * 1. 采用静态Gauge指标定义，确保指标注册的单例性
 * 2. 使用守护线程进行后台指标收集，避免阻塞应用主线程
 * 3. 支持多线程池指标的标签化区分，通过pool标签区分不同线程池
 * 4. 固定5秒采集周期，平衡实时性和系统开销
 *
 * 指标定义：
 * - threadpool_active_threads：线程池活跃线程数（Gauge类型）
 * - threadpool_queue_size：线程池队列大小（Gauge类型）
 * - 标签维度：pool（线程池名称）
 *
 * HTTP端点：
 * - 默认提供/metrics路径，符合Prometheus标准
 * - 支持Prometheus服务器定期抓取
 * - 文本格式输出，便于调试和监控集成
 *
 * 线程模型：
 * - 监控线程设置为守护线程，应用退出时自动终止
 * - 无限循环设计，确保指标持续更新
 * - 异常处理采用忽略策略，保证线程不中断
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 */
public class PrometheusExporter {

    /**
     * 活跃线程数指标
     *
     * 指标详情：
     * - 名称：threadpool_active_threads
     * - 类型：Gauge（可增可减的瞬时数值）
     * - 帮助信息：Active threads
     * - 标签：pool（标识不同线程池）
     *
     * 使用场景：
     * 监控线程池中当前正在执行任务的线程数量，反映线程池的繁忙程度
     */
    private static final Gauge activeThreads = Gauge.build()
            .name("threadpool_active_threads").help("Active threads").labelNames("pool").register();

    /**
     * 队列大小指标
     *
     * 指标详情：
     * - 名称：threadpool_queue_size
     * - 类型：Gauge（可增可减的瞬时数值）
     * - 帮助信息：Queue size
     * - 标签：pool（标识不同线程池）
     *
     * 使用场景：
     * 监控线程池任务队列中等待执行的任务数量，用于评估系统负载和积压情况
     */
    private static final Gauge queueSize = Gauge.build()
            .name("threadpool_queue_size").help("Queue size").labelNames("pool").register();

    /**
     * 监控线程实例
     *
     * 生命周期管理：
     * - 在start()方法中创建并启动
     * - 设置为守护线程，随应用生命周期结束
     * - 无限循环执行指标收集任务
     */
    private Thread monitorThread;

    /**
     * 启动Prometheus导出器
     *
     * 功能流程：
     * 1. 创建并启动HTTPServer，监听指定端口
     * 2. 创建监控线程，设置为守护线程
     * 3. 启动后台指标收集循环
     *
     * 指标收集逻辑：
     * - 每5秒遍历ThreadPoolManager中的所有线程池
     * - 获取每个线程池的PoolMetrics指标对象
     * - 更新对应的Prometheus Gauge指标值
     * - 使用pool标签区分不同线程池的指标
     *
     * 异常处理：
     * - HTTPServer创建失败时抛出异常，由调用方处理
     * - 监控线程内部采用忽略异常策略，确保线程不中断
     * - Thread.sleep()中断异常被忽略，保持循环继续
     *
     * 性能考虑：
     * - 5秒采集周期平衡实时性和系统开销
     * - 单次遍历所有线程池，确保数据一致性
     * - 守护线程设计避免影响应用正常退出
     *
     * @param port HTTP服务器监听端口，通常为9090
     * @throws Exception 当HTTPServer启动失败时抛出
     *
     * 使用示例：
     * PrometheusExporter exporter = new PrometheusExporter();
     * exporter.start(9090); // 在9090端口启动指标服务
     *
     * Prometheus配置：
     * 在prometheus.yml中添加：
     * scrape_configs:
     *   - job_name: 'smart-thread-pool'
     *     static_configs:
     *       - targets: ['localhost:9090']
     */
    public void start(int port) throws Exception {
        new HTTPServer(port);

        monitorThread = new Thread(() -> {
            while (true) {
                ThreadPoolManager.getAllPools().forEach(pool -> {
                    PoolMetrics metrics = pool.getMetrics();
                    activeThreads.labels(metrics.getPoolName()).set(metrics.getActiveCount());
                    queueSize.labels(metrics.getPoolName()).set(metrics.getQueueSize());
                });
                try { Thread.sleep(5000); } catch (Exception ignored) {}
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
}