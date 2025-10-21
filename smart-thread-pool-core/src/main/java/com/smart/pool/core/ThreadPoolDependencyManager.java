package com.smart.pool.core;

import java.util.*;

/**
 * 管理多个线程池之间的依赖关系
 * 例如 A 线程池完成任务后通知 B 线程池
 */
public class ThreadPoolDependencyManager {

    // key: 线程池名称, value: 依赖于该线程池完成的线程池列表
    private static final Map<String, List<String>> dependencies = new HashMap<>();

    /**
     * 添加依赖关系: fromPool 完成后通知 toPool
     */
    public static void addDependency(String fromPool, String toPool) {
        dependencies.computeIfAbsent(fromPool, k -> new ArrayList<>()).add(toPool);
    }

    /**
     * 某线程池任务完成后通知依赖它的线程池
     */
    public static void notifyCompletion(String poolName) {
        List<String> dependents = dependencies.get(poolName);
        if (dependents != null) {
            for (String dependentPool : dependents) {
                DynamicThreadPoolExecutor executor = ThreadPoolManager.get(dependentPool);
                if (executor != null) {
                    // 可以触发回调或自定义逻辑，这里仅打印示例
                    System.out.printf("Pool [%s] notified by completion of [%s]%n", dependentPool, poolName);
                }
            }
        }
    }
}
