package com.smart.pool.core.metrics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PoolMetrics implements PoolMetricsMBean {
    private String poolName;
    private int activeCount;
    private int corePoolSize;
    private int maximumPoolSize;
    private long completedTaskCount;
    private long taskCount;
    private int queueSize;

    public PoolMetrics(String poolName) {
        this.poolName = poolName;
    }

    public PoolMetrics(String poolName, int activeCount, int corePoolSize, int maximumPoolSize,
                       int queueSize, long completedTaskCount) {
        this.poolName = poolName;
        this.activeCount = activeCount;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.queueSize = queueSize;
        this.completedTaskCount = completedTaskCount;
    }
}
