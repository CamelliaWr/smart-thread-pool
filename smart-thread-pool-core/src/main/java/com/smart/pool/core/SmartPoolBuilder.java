package com.smart.pool.core;

import com.smart.pool.core.reject.RejectedStrategyManager;

import java.util.concurrent.*;

/**
 * 智能线程池构建器
 * <p>
 * 采用建造者模式（Builder Pattern）构建线程池，提供链式调用的API来配置线程池参数。
 * 支持自定义核心线程数、最大线程数、队列容量、线程工厂、拒绝策略等参数，并提供合理的默认值。
 * 构建的线程池会自动注册到ThreadPoolManager中进行统一管理。
 * </p>
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 */
public class SmartPoolBuilder {

    /**
     * 线程池名称，用于标识和日志记录
     */
    private String name;

    /**
     * 核心线程数，默认4个
     */
    private int corePoolSize = 4;

    /**
     * 最大线程数，默认8个
     */
    private int maxPoolSize = 8;

    /**
     * 队列容量，默认100个任务
     */
    private int queueCapacity = 100;

    /**
     * 线程空闲存活时间，默认60秒
     */
    private long keepAliveTime = 60L;

    /**
     * 是否允许核心线程超时销毁，默认false
     */
    private boolean allowCoreThreadTimeOut = false;

    /**
     * 工作队列，如果未指定则使用LinkedBlockingQueue
     */
    private BlockingQueue<Runnable> workQueue;

    /**
     * 线程工厂，如果未指定则使用CustomThreadFactory
     */
    private ThreadFactory threadFactory;

    /**
     * 拒绝执行处理器，如果未指定则使用RetryRejectedExecutionHandler
     */
    private RejectedExecutionHandler rejectedHandler;

    /**
     * 创建线程池构建器实例
     *
     * @param name 线程池名称，不能为空
     * @return 新的SmartPoolBuilder实例
     * @throws IllegalArgumentException 如果名称为空
     */
    public static SmartPoolBuilder create(String name) {
        SmartPoolBuilder builder = new SmartPoolBuilder();
        builder.name = name;
        return builder;
    }

    /**
     * 设置核心线程数
     *
     * @param corePoolSize 核心线程数，必须大于0
     * @return 当前构建器实例，支持链式调用
     */
    public SmartPoolBuilder corePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    /**
     * 设置最大线程数
     *
     * @param maxPoolSize 最大线程数，必须大于等于核心线程数
     * @return 当前构建器实例，支持链式调用
     */
    public SmartPoolBuilder maxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    /**
     * 设置队列容量
     *
     * @param queueCapacity 队列容量，必须大于0
     * @return 当前构建器实例，支持链式调用
     */
    public SmartPoolBuilder queueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        return this;
    }

    /**
     * 设置线程空闲存活时间
     *
     * @param keepAliveTime 存活时间（秒），必须大于等于0
     * @return 当前构建器实例，支持链式调用
     */
    public SmartPoolBuilder keepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    /**
     * 设置是否允许核心线程超时销毁
     *
     * @param allowCoreThreadTimeOut true表示允许核心线程超时销毁
     * @return 当前构建器实例，支持链式调用
     */
    public SmartPoolBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        return this;
    }

    /**
     * 设置自定义工作队列
     *
     * @param workQueue 工作队列实例，不能为null
     * @return 当前构建器实例，支持链式调用
     */
    public SmartPoolBuilder workQueue(BlockingQueue<Runnable> workQueue) {
        this.workQueue = workQueue;
        return this;
    }

    /**
     * 设置线程工厂
     *
     * @param threadFactory 线程工厂实例，不能为null
     * @return 当前构建器实例，支持链式调用
     */
    public SmartPoolBuilder threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * 设置拒绝执行处理器
     * <p>
     * 如果传入null，则使用SPI机制加载所有自定义拒绝策略
     * </p>
     *
     * @param handler 拒绝执行处理器，可以为null
     * @return 当前构建器实例，支持链式调用
     */
    public SmartPoolBuilder rejectedHandler(RejectedExecutionHandler handler) {
        if (handler != null) {
            this.rejectedHandler = handler;
        } else {
            // 使用 SPI 加载所有自定义拒绝策略
            this.rejectedHandler = RejectedStrategyManager::handleRejected;
        }
        return this;
    }

    /**
     * 构建动态线程池执行器
     * <p>
     * 根据配置的参数创建DynamicThreadPoolExecutor实例，并注册到ThreadPoolManager。
     * 如果某些参数未指定，会使用合理的默认值。
     * </p>
     *
     * @return 配置完成的DynamicThreadPoolExecutor实例
     * @throws IllegalStateException 如果构建过程中发生错误
     */
    public DynamicThreadPoolExecutor build() {
        // 如果未指定工作队列，使用LinkedBlockingQueue
        if (workQueue == null) {
            workQueue = new LinkedBlockingQueue<>(queueCapacity);
        }

        // 如果未指定线程工厂，使用自定义线程工厂
        if (threadFactory == null) {
            threadFactory = new CustomThreadFactory(name);
        }

        // 如果未指定拒绝处理器，使用重试拒绝处理器
        if (rejectedHandler == null) {
            rejectedHandler = new RetryRejectedExecutionHandler();
        }

        // 创建动态线程池执行器
        DynamicThreadPoolExecutor executor = new DynamicThreadPoolExecutor(
                name,
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                rejectedHandler
        );

        // 设置核心线程超时策略
        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);

        // 注册到线程池管理器
        ThreadPoolManager.register(name, executor);
        return executor;
    }

    /**
     * 自定义线程工厂
     * <p>
     * 创建具有统一命名规范的线程，设置为守护线程并添加异常处理器
     * </p>
     */
    static class CustomThreadFactory implements ThreadFactory {
        /**
         * 线程池名称
         */
        private final String poolName;

        /**
         * 线程计数器
         */
        private int count = 0;

        /**
         * 构造线程工厂
         *
         * @param poolName 线程池名称
         */
        CustomThreadFactory(String poolName) {
            this.poolName = poolName;
        }

        /**
         * 创建新线程
         *
         * @param r 要执行的任务
         * @return 新创建的线程
         */
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, poolName + "-thread-" + (++count));
            t.setDaemon(true);
            t.setUncaughtExceptionHandler((thread, throwable) ->
                    System.err.printf("[Thread %s] uncaught: %s%n", thread.getName(), throwable.getMessage()));
            return t;
        }
    }

    /**
     * 重试拒绝执行处理器
     * <p>
     * 当任务被拒绝执行时，尝试将任务重新放入队列，最多重试3次
     * </p>
     */
    static class RetryRejectedExecutionHandler implements RejectedExecutionHandler {
        /**
         * 重试次数，默认3次
         */
        private final int retries = 3;

        /**
         * 处理被拒绝执行的任务
         * <p>
         * 尝试将任务重新放入队列，最多重试3次，每次等待100毫秒
         * </p>
         *
         * @param r        被拒绝的任务
         * @param executor 执行该任务的线程池
         * @throws NullPointerException 如果任务为null
         */
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            int attempt = 0;
            boolean submitted = false;
            // 尝试重试放入队列
            while (attempt < retries && !submitted) {
                try {
                    // 等待100毫秒后尝试将任务放入队列
                    executor.getQueue().offer(r, 100, TimeUnit.MILLISECONDS);
                    submitted = true;
                } catch (InterruptedException ignored) {
                    // 中断异常，继续重试
                }
                attempt++;
            }
            // 如果最终仍未成功，打印错误信息
            if (!submitted) {
                System.err.printf("[ThreadPool %s] Task rejected after %d retries%n", executor.toString(), retries);
            }
        }
    }
}