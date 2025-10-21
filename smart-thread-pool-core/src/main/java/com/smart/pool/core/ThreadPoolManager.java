package com.smart.pool.core;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程池管理器
 * <p>
 * 提供线程池的统一注册、获取和管理功能。使用线程安全的ConcurrentHashMap存储所有线程池实例，
 * 支持通过名称快速获取特定的线程池，以及获取所有已注册的线程池集合。
 * 采用单例模式设计，通过静态方法提供全局访问点。
 * </p>
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 */
public class ThreadPoolManager {

    /**
     * 线程池存储映射
     * <p>
     * 使用ConcurrentHashMap保证线程安全，key为线程池名称，value为对应的DynamicThreadPoolExecutor实例
     * </p>
     */
    private static final Map<String, DynamicThreadPoolExecutor> POOLS = new ConcurrentHashMap<>();

    /**
     * 注册线程池
     * <p>
     * 将指定的线程池实例注册到管理器中，使用线程池名称作为唯一标识。
     * 如果已存在同名线程池，新的实例会覆盖旧的实例。
     * </p>
     *
     * @param name      线程池名称，作为唯一标识符
     * @param executor  要注册的线程池执行器实例
     * @return 注册的线程池执行器实例，便于链式调用
     * @throws NullPointerException 如果name或executor为null
     */
    public static DynamicThreadPoolExecutor register(String name, DynamicThreadPoolExecutor executor) {
        POOLS.put(name, executor);
        return executor;
    }

    /**
     * 根据名称获取线程池
     * <p>
     * 通过线程池名称快速获取对应的线程池执行器实例。
     * </p>
     *
     * @param name 线程池名称
     * @return 对应的线程池执行器实例，如果不存在则返回null
     * @throws NullPointerException 如果name为null
     */
    public static DynamicThreadPoolExecutor get(String name) {
        return POOLS.get(name);
    }

    /**
     * 获取所有已注册的线程池
     * <p>
     * 返回所有已注册线程池的集合视图，便于统一管理和监控。
     * </p>
     *
     * @return 所有线程池执行器实例的集合，如果没有任何注册则返回空集合
     */
    public static Collection<DynamicThreadPoolExecutor> getAllPools() {
        return POOLS.values();
    }

    /**
     * 移除指定名称的线程池
     * <p>
     * 从管理器中移除指定名称的线程池执行器。
     * </p>
     *
     * @param name 要移除的线程池名称
     * @return 被移除的线程池执行器实例，如果不存在则返回null
     * @throws NullPointerException 如果name为null
     */
    public static DynamicThreadPoolExecutor remove(String name) {
        return POOLS.remove(name);
    }

    /**
     * 检查指定名称的线程池是否存在
     * <p>
     * 判断管理器中是否已注册指定名称的线程池。
     * </p>
     *
     * @param name 线程池名称
     * @return 如果存在返回true，否则返回false
     * @throws NullPointerException 如果name为null
     */
    public static boolean contains(String name) {
        return POOLS.containsKey(name);
    }

    /**
     * 获取已注册的线程池数量
     * <p>
     * 返回当前管理器中已注册的线程池总数。
     * </p>
     *
     * @return 线程池数量
     */
    public static int size() {
        return POOLS.size();
    }

    /**
     * 清空所有线程池
     * <p>
     * 移除管理器中所有已注册的线程池执行器。
     * 注意：此方法不会自动关闭线程池，调用前请确保已正确关闭所有线程池。
     * </p>
     */
    public static void clear() {
        POOLS.clear();
    }
}