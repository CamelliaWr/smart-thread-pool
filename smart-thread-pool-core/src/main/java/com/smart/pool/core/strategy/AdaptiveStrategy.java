package com.smart.pool.core.strategy;

import com.smart.pool.core.DynamicThreadPoolExecutor;

/**
 * SPI 插件接口：线程池调节策略
 */
public interface AdaptiveStrategy {

    /**
     * 调整线程池
     */
    void adjust(DynamicThreadPoolExecutor executor);

    /**
     * 返回策略优先级，值越大越优先执行
     */
    default int getPriority() {
        return 0;
    }

    /**
     * 是否启用当前策略，可动态控制
     */
    default boolean isEnabled(DynamicThreadPoolExecutor executor) {
        return true;
    }
}
