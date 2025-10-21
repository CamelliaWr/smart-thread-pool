package com.smart.pool.core.metrics;

public interface PoolMetricsMBean {
    String getPoolName();
    int getActiveCount();
    int getCorePoolSize();
    int getMaximumPoolSize();
    int getQueueSize();
    long getCompletedTaskCount();
    long getTaskCount();
}
