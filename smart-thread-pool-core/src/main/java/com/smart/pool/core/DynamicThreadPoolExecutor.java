package com.smart.pool.core;

import com.smart.pool.core.metrics.PoolMetrics;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DynamicThreadPoolExecutor extends ThreadPoolExecutor {

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
