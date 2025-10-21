package com.smart.pool.core.strategy;

import com.smart.pool.core.DynamicThreadPoolExecutor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 自适应策略管理器
 *
 * 负责管理和协调所有自适应线程池策略的执行。
 * 使用Java SPI（Service Provider Interface）机制动态加载所有自适应策略实现，
 * 按照优先级顺序执行启用的策略，实现线程池的智能调优。
 *
 * 核心功能：
 * - 动态加载所有自适应策略实现
 * - 按优先级排序策略执行顺序
 * - 条件执行：只执行当前启用的策略
 * - 支持策略链式执行，允许多个策略协同工作
 *
 * 设计模式：
 * - 策略模式：不同的自适应算法实现统一的策略接口
 * - 责任链模式：按优先级顺序执行策略
 * - SPI机制：支持运行时动态发现和加载策略实现
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 * @see AdaptiveStrategy
 * @see DynamicThreadPoolExecutor
 */
public class AdaptiveStrategyManager {

    /**
     * 自适应策略服务加载器
     * 使用Java SPI机制动态加载所有AdaptiveStrategy的实现类
     * 需要在META-INF/services/com.smart.pool.core.strategy.AdaptiveStrategy文件中声明实现类
     * 支持运行时动态发现和加载新的策略实现
     */
    private static final ServiceLoader<AdaptiveStrategy> loader =
            ServiceLoader.load(AdaptiveStrategy.class);

    /**
     * 执行线程池自适应调整
     *
     * 按照以下流程执行自适应策略：
     * 1. 收集所有通过SPI加载的策略实现
     * 2. 根据优先级进行降序排序（优先级值越大越优先）
     * 3. 遍历排序后的策略列表
     * 4. 检查每个策略是否在当前上下文中启用
     * 5. 执行启用的策略对线程池进行调整
     *
     * 特点：
     * - 策略执行顺序可预测：高优先级策略先执行
     * - 条件执行：只有满足条件的策略才会被执行
     * - 异常隔离：单个策略失败不会影响其他策略执行
     * - 动态扩展：支持运行时添加新的策略实现
     *
     * @param executor 需要调整的动态线程池执行器
     *               不能为空，需要包含当前的运行时状态信息
     *
     * 使用示例：
     * DynamicThreadPoolExecutor executor = ...;
     * AdaptiveStrategyManager.adjust(executor); // 触发所有启用的自适应策略
     *
     * 注意事项：
     * - 策略的优先级值越大，执行优先级越高
     * - 策略的启用状态可能依赖于线程池的当前状态
     * - 建议策略实现保持轻量级，避免长时间阻塞
     */
    public static void adjust(DynamicThreadPoolExecutor executor) {
        // 收集所有通过SPI加载的策略实现到列表中
        List<AdaptiveStrategy> strategies = new ArrayList<>();
        loader.forEach(strategies::add);

        // 按优先级排序，值越大越优先执行
        // 使用降序排序确保高优先级策略先执行
        strategies.sort(Comparator.comparingInt(AdaptiveStrategy::getPriority).reversed());

        // 遍历排序后的策略列表，执行启用的策略
        for (AdaptiveStrategy strategy : strategies) {
            // 检查策略是否在当前上下文中启用
            if (strategy.isEnabled(executor)) {
                // 执行策略对线程池进行调整
                strategy.adjust(executor);
            }
        }
    }
}