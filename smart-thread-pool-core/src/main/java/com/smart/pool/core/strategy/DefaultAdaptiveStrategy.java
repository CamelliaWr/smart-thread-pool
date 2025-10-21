package com.smart.pool.core.strategy;

import com.smart.pool.core.DynamicThreadPoolExecutor;

public class DefaultAdaptiveStrategy implements AdaptiveStrategy {
    @Override
    public void adjustPool(DynamicThreadPoolExecutor executor) {
        int queueSize = executor.getQueue().size();
        int corePool = executor.getCorePoolSize();
        int maxPool = executor.getMaximumPoolSize();

        if(queueSize > maxPool * 2) {
            executor.setCorePoolSize(Math.min(corePool + 1, maxPool));
        } else if(queueSize < corePool / 2) {
            executor.setCorePoolSize(Math.max(1, corePool - 1));
        }
    }
}
