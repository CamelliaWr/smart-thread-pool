package com.smart.pool.core;

import java.util.*;

/**
 * 线程池依赖关系管理器
 * <p>
 * 管理多个线程池之间的依赖关系，实现线程池间的任务完成通知机制。
 * 例如：当A线程池完成任务后，可以通知依赖于A的B线程池执行相应的处理逻辑。
 * 支持一对多的依赖关系，一个线程池的完成可以触发多个依赖线程池的通知。
 * </p>
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 */
public class ThreadPoolDependencyManager {

    /**
     * 线程池依赖关系映射
     * <p>
     * key: 前置线程池名称（完成任务的线程池）
     * value: 依赖于该线程池完成的线程池名称列表
     * </p>
     */
    private static final Map<String, List<String>> dependencies = new HashMap<>();

    /**
     * 添加线程池依赖关系
     * <p>
     * 建立从fromPool到toPool的依赖关系，当fromPool中的任务完成后会通知toPool。
     * 支持一对多依赖，一个线程池可以触发多个依赖线程池的通知。
     * </p>
     *
     * @param fromPool 前置线程池名称，完成任务后触发通知
     * @param toPool   依赖线程池名称，接收通知的线程池
     * @throws NullPointerException 如果任一参数为null
     */
    public static void addDependency(String fromPool, String toPool) {
        dependencies.computeIfAbsent(fromPool, k -> new ArrayList<>()).add(toPool);
    }

    /**
     * 通知线程池任务完成
     * <p>
     * 当指定线程池完成任务时调用此方法，会通知所有依赖于该线程池的其他线程池。
     * 目前实现仅打印通知信息，可扩展为执行具体的回调逻辑。
     * </p>
     *
     * @param poolName 完成任务的前置线程池名称
     * @throws NullPointerException 如果参数为null
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

    /**
     * 获取线程池的所有依赖关系
     * <p>
     * 返回当前注册的所有依赖关系的只读视图
     * </p>
     *
     * @return 不可修改的依赖关系映射副本
     */
    public static Map<String, List<String>> getDependencies() {
        return Collections.unmodifiableMap(dependencies);
    }

    /**
     * 移除线程池依赖关系
     * <p>
     * 移除指定的线程池依赖关系
     * </p>
     *
     * @param fromPool 前置线程池名称
     * @param toPool   依赖线程池名称
     * @return 如果依赖关系存在并被移除返回true，否则返回false
     */
    public static boolean removeDependency(String fromPool, String toPool) {
        List<String> dependents = dependencies.get(fromPool);
        if (dependents != null) {
            return dependents.remove(toPool);
        }
        return false;
    }

    /**
     * 清空所有依赖关系
     * <p>
     * 移除所有已注册的线程池依赖关系
     * </p>
     */
    public static void clearDependencies() {
        dependencies.clear();
    }
}