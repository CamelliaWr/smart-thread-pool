package com.smart.pool.core.strategy;

import com.smart.pool.core.DynamicThreadPoolExecutor;

public interface AdaptiveStrategy {
    void adjustPool(DynamicThreadPoolExecutor executor);
}
