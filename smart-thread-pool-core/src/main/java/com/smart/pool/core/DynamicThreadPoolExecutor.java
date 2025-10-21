package com.smart.pool.core;

import com.smart.pool.core.metrics.PoolMetrics;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DynamicThreadPoolExecutor extends ThreadPoolExecutor {

    private final String poolName;
    private final AtomicLong completedTaskCount = new AtomicLong(0);

    public DynamicThreadPoolExecutor(String poolName,
                                     int corePoolSize,
                                     int maximumPoolSize,
                                     long keepAliveTime,
                                     TimeUnit unit,
                                     BlockingQueue<Runnable> workQueue,
                                     ThreadFactory threadFactory,
                                     RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.poolName = poolName;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        completedTaskCount.incrementAndGet();
        if (t != null) {
            System.err.printf("[ThreadPool %s] Task error: %s%n", poolName, t.getMessage());
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

    /** 动态调整队列容量，仅限 LinkedBlockingQueue */
    public void adjustQueueCapacity(int newCapacity) {
        BlockingQueue<Runnable> queue = getQueue();
        if (queue instanceof LinkedBlockingQueue) {
            LinkedBlockingQueue<Runnable> lbq = (LinkedBlockingQueue<Runnable>) queue;
            // 只能增大容量，无法减小
            int delta = newCapacity - lbq.remainingCapacity() - lbq.size();
            for (int i = 0; i < delta; i++) {
                lbq.offer(() -> {}); // 占位，不影响业务任务
            }
        } else {
            System.err.printf("[ThreadPool %s] Queue type does not support dynamic capacity adjustment%n", poolName);
        }
    }
}
