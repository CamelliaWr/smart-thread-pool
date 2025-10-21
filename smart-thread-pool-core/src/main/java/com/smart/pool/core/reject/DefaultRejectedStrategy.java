package com.smart.pool.core.reject;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 默认拒绝策略处理器
 *
 * 当线程池无法接受新任务时，该策略会尝试将任务重新提交到队列中。
 * 通过重试机制提高任务提交的成功率，避免任务直接被丢弃。
 *
 * 策略特点：
 * - 最多重试3次
 * - 每次重试间隔100毫秒
 * - 如果重试失败，打印错误信息但不抛出异常
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 * @see RejectedExecutionStrategy
 */
public class DefaultRejectedStrategy implements RejectedExecutionStrategy {

    /**
     * 最大重试次数
     * 当任务被拒绝时，最多尝试重新提交的次数
     */
    private final int retries = 3;

    /**
     * 处理被拒绝的任务
     *
     * 实现重试机制：当线程池队列满时，尝试多次将任务重新加入队列。
     * 使用offer方法配合超时时间，避免无限阻塞。
     *
     * @param r 被拒绝执行的任务
     * @param executor 触发拒绝操作的线程池执行器
     *
     * 处理流程：
     * 1. 初始化重试计数器和提交状态
     * 2. 在最大重试次数内循环尝试提交任务
     * 3. 每次尝试等待100毫秒将任务加入队列
     * 4. 如果提交成功则退出循环
     * 5. 如果所有重试都失败，打印拒绝信息到标准错误输出
     *
     * 注意：该策略不会抛出异常，而是静默处理失败情况
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // 初始化重试计数器和任务提交状态
        int attempt = 0;
        boolean submitted = false;

        // 在最大重试次数内循环尝试提交任务
        while (attempt < retries && !submitted) {
            try {
                // 尝试将任务加入队列，等待100毫秒
                submitted = executor.getQueue().offer(r, 100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
                // 线程被中断时忽略异常，继续下一次重试
            }
            attempt++;
        }

        // 如果所有重试都失败，打印拒绝信息
        if (!submitted) {
            System.err.printf("[ThreadPool %s] Task rejected after %d retries%n",
                    executor.toString(), retries);
        }
    }
}