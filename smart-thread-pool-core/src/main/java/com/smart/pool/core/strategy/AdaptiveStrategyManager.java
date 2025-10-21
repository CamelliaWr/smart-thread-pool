package com.smart.pool.core.strategy;

import com.smart.pool.core.DynamicThreadPoolExecutor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * SPI 管理器：按优先级组合执行所有策略
 */
public class AdaptiveStrategyManager {

    private static final ServiceLoader<AdaptiveStrategy> loader = ServiceLoader.load(AdaptiveStrategy.class);

    public static void adjust(DynamicThreadPoolExecutor executor) {
        List<AdaptiveStrategy> strategies = new ArrayList<>();
        loader.forEach(strategies::add);

        // 按优先级排序，值越大越优先执行
        strategies.sort(Comparator.comparingInt(AdaptiveStrategy::getPriority).reversed());

        for (AdaptiveStrategy strategy : strategies) {
            if (strategy.isEnabled(executor)) {
                strategy.adjust(executor);
            }
        }
    }
}
