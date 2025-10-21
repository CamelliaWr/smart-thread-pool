package com.smart.pool.core;

import com.smart.pool.core.metrics.PoolMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DynamicThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger log = LoggerFactory.getLogger(DynamicThreadPoolExecutor.class);

    private final String poolName;
    private final AtomicLong completedTaskCount = new AtomicLong(0);

    public DynamicThreadPoolExecutor(String poolName, int corePoolSize, int maximumPoolSize,
                                     long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                     ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.poolName = poolName;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        completedTaskCount.incrementAndGet();

        int active = getActiveCount();
        int queueSize = getQueue().size();

        if (active >= getMaximumPoolSize() * 0.9) {
            log.warn("[HIGH_LOAD][ThreadPool:{}] 活跃线程接近上限: active={}, max={}", poolName, active, getMaximumPoolSize());
        }

        if (queueSize >= getQueue().remainingCapacity() * 0.1) {
            log.warn("[QUEUE_FULL][ThreadPool:{}] 队列接近满载: size={}, remaining={}", poolName, queueSize, getQueue().remainingCapacity());
        }

        // 通知依赖线程池（如果有依赖管理）
        ThreadPoolDependencyManager.notifyCompletion(poolName);
    }

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

    public String getPoolName() {
        return poolName;
    }

    public long getCompletedTaskCountAtomic() {
        return completedTaskCount.get();
    }

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
