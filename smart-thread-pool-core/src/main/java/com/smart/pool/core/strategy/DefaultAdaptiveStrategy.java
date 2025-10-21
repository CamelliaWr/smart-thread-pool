package com.smart.pool.core.strategy;

import com.smart.pool.core.DynamicThreadPoolExecutor;

/**
 * 默认自适应线程池调节策略
 *
 * 基于队列大小的简单自适应调节策略，通过监控线程池任务队列的长度来动态调整核心线程数。
 * 该策略旨在保持线程池的高效运行，避免资源浪费或任务积压。
 *
 * 调节逻辑：
 * - 当队列大小超过最大线程数2倍时：增加核心线程数
 * - 当队列大小小于核心线程数一半时：减少核心线程数
 * - 其他情况：保持当前配置不变
 *
 * 设计特点：
 * - 保守调节：每次只调整1个线程，避免剧烈波动
 * - 边界保护：确保核心线程数在有效范围内 [1, maxPoolSize]
 * - 简单高效：基于队列大小的轻量级判断逻辑
 * - 通用性强：适用于大多数常规业务场景
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 * @see AdaptiveStrategy
 * @see DynamicThreadPoolExecutor
 */
public class DefaultAdaptiveStrategy implements AdaptiveStrategy {

    /**
     * 执行线程池自适应调节
     *
     * 根据当前线程池状态动态调整核心线程数，调节规则：
     * 1. 获取当前队列大小、核心线程数和最大线程数
     * 2. 如果队列大小 > 最大线程数 * 2：增加核心线程数（不超过最大值）
     * 3. 如果队列大小 < 核心线程数 / 2：减少核心线程数（不低于1）
     * 4. 其他情况：保持当前配置不变
     *
     * 调节目标：在任务处理能力和资源消耗之间找到平衡点
     *
     * @param executor 需要调节的动态线程池执行器
     *                必须包含有效的运行时状态信息
     *
     * 线程安全：
     * 该方法设计为线程安全，可以被多个线程同时调用
     *
     * 性能考虑：
     * - 调节操作相对轻量级，不会显著影响线程池性能
     * - 建议配合适当的调节间隔，避免过于频繁的调节
     */
    @Override
    public void adjust(DynamicThreadPoolExecutor executor) {
        // 获取当前线程池状态信息
        int queueSize = executor.getQueue().size();      // 当前队列大小
        int corePool = executor.getCorePoolSize();      // 当前核心线程数
        int maxPool = executor.getMaximumPoolSize();    // 最大线程数限制

        // 队列积压严重，需要增加处理能力
        if (queueSize > maxPool * 2) {
            // 增加核心线程数，但不超过最大线程数限制
            executor.setCorePoolSize(Math.min(corePool + 1, maxPool));
        }
        // 队列空闲，可以减少资源消耗
        else if (queueSize < corePool / 2) {
            // 减少核心线程数，但不低于1（确保至少有一个线程）
            executor.setCorePoolSize(Math.max(1, corePool - 1));
        }
        // 其他情况：保持当前配置不变
    }

    /**
     * 获取策略优先级
     *
     * @return 优先级值，值越大优先级越高
     *         默认策略返回0，确保作为最低优先级策略
     *
     * 优先级机制：
     * 在策略链中，高优先级策略先执行，可以作为后续策略的基准或后备
     */
    @Override
    public int getPriority() {
        return 0; // 默认优先级最低，作为基础后备策略
    }

    /**
     * 检查策略是否启用
     *
     * @param executor 线程池执行器（用于上下文判断）
     * @return true 表示策略始终启用，false 表示策略被禁用
     *
     * 默认策略始终返回true，确保在任何情况下都能提供基本的自适应能力
     * 子类可以重写此方法，根据特定条件动态启用或禁用策略
     */
    @Override
    public boolean isEnabled(DynamicThreadPoolExecutor executor) {
        return true; // 默认始终启用，提供基础调节能力
    }
}