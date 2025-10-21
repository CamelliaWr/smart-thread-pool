package com.smart.pool.core.reject;

import java.util.ServiceLoader;
import java.util.concurrent.ThreadPoolExecutor;

public class RejectedStrategyManager {

    private static final ServiceLoader<RejectedExecutionStrategy> loader = ServiceLoader.load(RejectedExecutionStrategy.class);

    public static void handleRejected(Runnable task, ThreadPoolExecutor executor) {
        boolean handled = false;
        for (RejectedExecutionStrategy strategy : loader) {
            try {
                strategy.rejectedExecution(task, executor);
                handled = true;
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!handled) {
            // 使用默认策略
            new DefaultRejectedStrategy().rejectedExecution(task, executor);
        }
    }
}
