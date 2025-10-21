package com.smart.pool.core.metrics;

import lombok.Getter;
import lombok.Setter;

/**
 * 线程池指标数据类
 * 用于收集和存储线程池的运行时状态信息，实现PoolMetricsMBean接口以支持JMX监控
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 */
@Getter
@Setter
public class PoolMetrics implements PoolMetricsMBean {

    /**
     * 线程池名称
     * 用于标识和区分不同的线程池实例
     */
    private String poolName;

    /**
     * 当前活动线程数
     * 正在执行任务的线程数量
     */
    private int activeCount;

    /**
     * 核心线程数
     * 线程池维护的最小线程数量，即使它们处于空闲状态也不会被回收
     */
    private int corePoolSize;

    /**
     * 最大线程数
     * 线程池允许创建的最大线程数量
     */
    private int maximumPoolSize;

    /**
     * 已完成任务数
     * 线程池从开始运行到现在已完成的任务总数
     */
    private long completedTaskCount;

    /**
     * 总任务数
     * 线程池从开始运行到现在接收到的任务总数（包括已完成和正在执行的）
     */
    private long taskCount;

    /**
     * 队列大小
     * 当前等待执行的任务队列中的任务数量
     */
    private int queueSize;

    /**
     * 构造函数 - 创建指定名称的线程池指标对象
     *
     * @param poolName 线程池名称，不能为空
     */
    public PoolMetrics(String poolName) {
        this.poolName = poolName;
    }

    /**
     * 构造函数 - 创建完整的线程池指标对象
     *
     * @param poolName 线程池名称
     * @param activeCount 当前活动线程数
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param queueSize 队列大小
     * @param completedTaskCount 已完成任务数
     */
    public PoolMetrics(String poolName, int activeCount, int corePoolSize, int maximumPoolSize,
                       int queueSize, long completedTaskCount) {
        this.poolName = poolName;
        this.activeCount = activeCount;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.queueSize = queueSize;
        this.completedTaskCount = completedTaskCount;
    }
}