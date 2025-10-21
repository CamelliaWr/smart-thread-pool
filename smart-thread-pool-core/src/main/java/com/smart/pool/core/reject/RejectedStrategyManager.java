package com.smart.pool.core.reject;

import java.util.ServiceLoader;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 拒绝策略管理器
 *
 * 负责管理和协调所有可用的任务拒绝处理策略。
 * 使用Java SPI（Service Provider Interface）机制动态加载拒绝策略实现，
 * 提供统一的入口点来处理线程池任务被拒绝的情况。
 *
 * 功能特点：
 * - 支持动态加载多个拒绝策略实现
 * - 按顺序尝试各个策略，直到成功处理
 * - 提供默认策略作为后备方案
 * - 异常安全，单个策略失败不会影响其他策略
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 * @see RejectedExecutionStrategy
 * @see DefaultRejectedStrategy
 */
public class RejectedStrategyManager {

    /**
     * 拒绝策略服务加载器
     * 使用Java SPI机制动态加载所有RejectedExecutionStrategy的实现类
     * 需要在META-INF/services/com.smart.pool.core.reject.RejectedExecutionStrategy文件中声明实现类
     */
    private static final ServiceLoader<RejectedExecutionStrategy> loader =
            ServiceLoader.load(RejectedExecutionStrategy.class);

    /**
     * 处理被拒绝的任务
     *
     * 按照以下流程处理被拒绝的任务：
     * 1. 遍历所有通过SPI加载的拒绝策略
     * 2. 依次尝试每个策略处理任务
     * 3. 第一个成功处理的策略会终止后续尝试
     * 4. 如果所有策略都失败，使用默认策略作为后备
     *
     * @param task 被拒绝执行的任务，不能为空
     * @param executor 触发拒绝操作的线程池执行器，不能为空
     *
     * 异常处理：
     * - 单个策略执行失败时会打印异常堆栈，但不会中断整体流程
     * - 确保至少有一个策略（默认策略）会被执行
     *
     * 线程安全：
     * 该方法为线程安全，可以被多个线程同时调用
     */
    public static void handleRejected(Runnable task, ThreadPoolExecutor executor) {
        // 标记是否有策略成功处理了任务
        boolean handled = false;

        // 遍历所有通过SPI加载的拒绝策略
        for (RejectedExecutionStrategy strategy : loader) {
            try {
                // 尝试使用当前策略处理被拒绝的任务
                strategy.rejectedExecution(task, executor);
                // 如果策略执行成功（没有抛出异常），标记为已处理
                handled = true;
                // 成功处理后立即退出，不尝试后续策略
                break;
            } catch (Exception e) {
                // 策略执行失败时打印异常信息，但继续尝试下一个策略
                e.printStackTrace();
            }
        }

        // 如果所有策略都未能成功处理，使用默认策略作为后备方案
        if (!handled) {
            // 使用默认策略处理任务，确保任务至少被处理一次
            new DefaultRejectedStrategy().rejectedExecution(task, executor);
        }
    }
}