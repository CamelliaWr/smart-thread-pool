package com.smart.pool.core.strategy;

import com.smart.pool.core.DynamicThreadPoolExecutor;

/**
 * 默认线程池调节策略
 */
public class DefaultAdaptiveStrategy implements AdaptiveStrategy {

    @Override
    public void adjust(DynamicThreadPoolExecutor executor) {
        int queueSize = executor.getQueue().size();
        int corePool = executor.getCorePoolSize();
        int maxPool = executor.getMaximumPoolSize();

        if (queueSize > maxPool * 2) {
            executor.setCorePoolSize(Math.min(corePool + 1, maxPool));
        } else if (queueSize < corePool / 2) {
            executor.setCorePoolSize(Math.max(1, corePool - 1));
        }
    }

    @Override
    public int getPriority() {
        return 0; // 默认优先级最低
    }

    @Override
    public boolean isEnabled(DynamicThreadPoolExecutor executor) {
        return true; // 默认始终启用
    }
}
