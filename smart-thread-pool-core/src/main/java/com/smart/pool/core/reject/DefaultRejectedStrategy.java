package com.smart.pool.core.reject;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 默认重试策略
 */
public class DefaultRejectedStrategy implements RejectedExecutionStrategy {

    private final int retries = 3;

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        int attempt = 0;
        boolean submitted = false;
        while (attempt < retries && !submitted) {
            try {
                submitted = executor.getQueue().offer(r, 100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {}
            attempt++;
        }
        if (!submitted) {
            System.err.printf("[ThreadPool %s] Task rejected after %d retries%n", executor.toString(), retries);
        }
    }
}
