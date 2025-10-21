package com.smart.pool.core;

import java.util.concurrent.*;

public class SmartPoolBuilder {
    private String name;
    private int corePoolSize = 4;
    private int maxPoolSize = 8;
    private int queueCapacity = 100;
    private long keepAliveTime = 60L;
    private boolean allowCoreThreadTimeOut = false;

    public static SmartPoolBuilder create(String name){
        SmartPoolBuilder builder = new SmartPoolBuilder();
        builder.name = name;
        return builder;
    }

    public SmartPoolBuilder corePoolSize(int corePoolSize){ this.corePoolSize = corePoolSize; return this; }
    public SmartPoolBuilder maxPoolSize(int maxPoolSize){ this.maxPoolSize = maxPoolSize; return this; }
    public SmartPoolBuilder queueCapacity(int queueCapacity){ this.queueCapacity = queueCapacity; return this; }
    public SmartPoolBuilder keepAliveTime(int keepAliveTime){ this.keepAliveTime = keepAliveTime; return this; }
    public SmartPoolBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut){ this.allowCoreThreadTimeOut = allowCoreThreadTimeOut; return this; }

    public DynamicThreadPoolExecutor build(){
        DynamicThreadPoolExecutor executor = new DynamicThreadPoolExecutor(
                name,
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        ThreadPoolManager.register(name, executor);
        return executor;
    }
}
