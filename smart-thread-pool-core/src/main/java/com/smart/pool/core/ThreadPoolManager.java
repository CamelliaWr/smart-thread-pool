package com.smart.pool.core;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadPoolManager {

    private static final Map<String, DynamicThreadPoolExecutor> POOLS = new ConcurrentHashMap<>();

    public static DynamicThreadPoolExecutor register(String name, DynamicThreadPoolExecutor executor) {
        POOLS.put(name, executor);
        return executor;
    }

    public static DynamicThreadPoolExecutor get(String name) {
        return POOLS.get(name);
    }

    public static Collection<DynamicThreadPoolExecutor> getAllPools() {
        return POOLS.values();
    }
}
