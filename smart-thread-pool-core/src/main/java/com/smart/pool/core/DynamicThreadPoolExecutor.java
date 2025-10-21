package com.smart.pool.core;

import com.smart.pool.core.metrics.PoolMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 动态线程池执行器
 * <p>
 * 该类扩展了ThreadPoolExecutor，提供了线程池状态监控、指标收集和异常处理功能。
 * 支持线程池名称标识、任务完成计数、高负载预警和任务拒绝处理。
 * </p>
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 */
public class DynamicThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(DynamicThreadPoolExecutor.class);

    /**
     * 线程池名称，用于标识和日志记录
     */
    private final String poolName;

    /**
     * 已完成任务计数器，使用原子操作保证线程安全
     */
    private final AtomicLong completedTaskCount = new AtomicLong(0);

    /**
     * 构造动态线程池执行器
     *
     * @param poolName      线程池名称
     * @param corePoolSize  核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime 线程空闲存活时间
     * @param unit          时间单位
     * @param workQueue     工作队列
     * @param threadFactory 线程工厂
     * @param handler       拒绝执行处理器
     */
    public DynamicThreadPoolExecutor(String poolName, int corePoolSize, int maximumPoolSize,
                                     long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                     ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.poolName = poolName;
    }

    /**
     * 任务执行完成后的回调方法
     * <p>
     * 在任务执行完成后更新计数器，检查线程池负载状态，
     * 当活跃线程数达到最大线程数的90%或队列使用率超过90%时记录警告日志。
     * </p>
     *
     * @param r 已执行的任务
     * @param t 执行过程中抛出的异常，如果没有异常则为null
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        // 增加已完成任务计数
        completedTaskCount.incrementAndGet();

        int active = getActiveCount();
        int queueSize = getQueue().size();

        // 检查活跃线程数是否接近上限（90%阈值）
        if (active >= getMaximumPoolSize() * 0.9) {
            log.warn("[HIGH_LOAD][ThreadPool:{}] 活跃线程接近上限: active={}, max={}", poolName, active, getMaximumPoolSize());
        }

        // 检查队列是否接近满载（90%阈值）
        if (queueSize >= getQueue().remainingCapacity() * 0.1) {
            log.warn("[QUEUE_FULL][ThreadPool:{}] 队列接近满载: size={}, remaining={}", poolName, queueSize, getQueue().remainingCapacity());
        }

        // 通知依赖线程池（如果有依赖管理）
        ThreadPoolDependencyManager.notifyCompletion(poolName);
    }

    /**
     * 执行任务的方法
     * <p>
     * 包装了父类的execute方法，添加了异常处理和日志记录功能。
     * 当任务被拒绝执行时记录错误日志并可触发告警系统。
     * </p>
     *
     * @param command 要执行的任务
     * @throws RejectedExecutionException 当任务无法被执行时抛出
     */
    @Override
    public void execute(Runnable command) {
        try {
            super.execute(command);
        } catch (RejectedExecutionException e) {
            log.error("[REJECTED][ThreadPool:{}] 任务被拒绝: {}", poolName, command, e);
            // 可在这里触发告警系统，例如 webhook / 邮件
            throw e; // 保持原本行为
        }
    }

    /**
     * 获取线程池名称
     *
     * @return 线程池名称
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * 获取原子性的已完成任务数
     *
     * @return 已完成任务总数
     */
    public long getCompletedTaskCountAtomic() {
        return completedTaskCount.get();
    }

    /**
     * 获取线程池指标
     * <p>
     * 创建并返回包含当前线程池各项指标的PoolMetrics对象，
     * 包括活跃线程数、核心线程数、最大线程数、队列大小和已完成任务数。
     * </p>
     *
     * @return 线程池指标对象
     */
    public PoolMetrics getMetrics() {
        PoolMetrics metrics = new PoolMetrics(
                poolName,
                getActiveCount(),
                getCorePoolSize(),
                getMaximumPoolSize(),
                getQueue().size(),
                getCompletedTaskCountAtomic()
        );
        metrics.setTaskCount(getTaskCount());
        return metrics;
    }
}