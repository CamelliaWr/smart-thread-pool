package com.smart.pool.core;

import com.smart.pool.core.reject.RejectedStrategyManager;

import java.util.concurrent.*;

public class SmartPoolBuilder {

    private String name;
    private int corePoolSize = 4;
    private int maxPoolSize = 8;
    private int queueCapacity = 100;
    private long keepAliveTime = 60L;
    private boolean allowCoreThreadTimeOut = false;
    private BlockingQueue<Runnable> workQueue;
    private ThreadFactory threadFactory;
    private RejectedExecutionHandler rejectedHandler;

    public static SmartPoolBuilder create(String name) {
        SmartPoolBuilder builder = new SmartPoolBuilder();
        builder.name = name;
        return builder;
    }

    public SmartPoolBuilder corePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public SmartPoolBuilder maxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public SmartPoolBuilder queueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        return this;
    }

    public SmartPoolBuilder keepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    public SmartPoolBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        return this;
    }

    public SmartPoolBuilder workQueue(BlockingQueue<Runnable> workQueue) {
        this.workQueue = workQueue;
        return this;
    }

    public SmartPoolBuilder threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public SmartPoolBuilder rejectedHandler(RejectedExecutionHandler handler) {
        if (handler != null) {
            this.rejectedHandler = handler;
        } else {
            // 使用 SPI 加载所有自定义拒绝策略
            this.rejectedHandler = RejectedStrategyManager::handleRejected;
        }
        return this;
    }


    public DynamicThreadPoolExecutor build() {
        if (workQueue == null) {
            workQueue = new LinkedBlockingQueue<>(queueCapacity);
        }
        if (threadFactory == null) {
            threadFactory = new CustomThreadFactory(name);
        }
        if (rejectedHandler == null) {
            rejectedHandler = new RetryRejectedExecutionHandler();
        }

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

        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        ThreadPoolManager.register(name, executor);
        return executor;
    }

    /** 默认线程工厂，命名线程+守护+异常处理 */
    static class CustomThreadFactory implements ThreadFactory {
        private final String poolName;
        private int count = 0;

        CustomThreadFactory(String poolName) {
            this.poolName = poolName;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, poolName + "-thread-" + (++count));
            t.setDaemon(true);
            t.setUncaughtExceptionHandler((thread, throwable) ->
                    System.err.printf("[Thread %s] uncaught: %s%n", thread.getName(), throwable.getMessage()));
            return t;
        }
    }

    /** 默认拒绝策略，重试3次 */
    static class RetryRejectedExecutionHandler implements RejectedExecutionHandler {
        private final int retries = 3;

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            int attempt = 0;
            boolean submitted = false;
            while (attempt < retries && !submitted) {
                try {
                    executor.getQueue().offer(r, 100, TimeUnit.MILLISECONDS);
                    submitted = true;
                } catch (InterruptedException ignored) {}
                attempt++;
            }
            if (!submitted) {
                System.err.printf("[ThreadPool %s] Task rejected after %d retries%n", executor.toString(), retries);
            }
        }
    }
}
