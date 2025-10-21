package com.smart.pool.monitor;

import com.smart.pool.core.ThreadPoolManager;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * JMX指标导出器
 *
 * 功能说明：
 * 负责将智能线程池的运行指标注册为JMX MBean，使线程池状态可通过JMX进行监控和管理
 *
 * 设计特点：
 * 1. 采用静态工具类设计，提供统一的JMX指标导出功能
 * 2. 自动为每个线程池创建对应的MBean，使用标准化的命名规范
 * 3. 支持重复调用，通过isRegistered检查避免重复注册
 * 4. 异常处理机制确保单个线程池注册失败不会影响其他线程池
 *
 * MBean命名规范：
 * - 域名：smart.pool
 * - 类型：ThreadPool
 * - 名称：线程池名称（通过pool.getMetrics().getPoolName()获取）
 * - 完整格式：smart.pool:type=ThreadPool,name={poolName}
 *
 * 监控支持：
 * - 支持JConsole、VisualVM等JMX客户端工具连接查看
 * - 可通过JMX获取线程池的各项运行指标和状态信息
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 */
public class JmxMetricsExporter {

    /**
     * 导出所有线程池指标到JMX
     *
     * 执行流程：
     * 1. 获取平台MBean服务器实例
     * 2. 遍历ThreadPoolManager中注册的所有线程池
     * 3. 为每个线程池创建对应的ObjectName（遵循smart.pool:type=ThreadPool,name={poolName}规范）
     * 4. 检查MBean是否已注册，避免重复注册
     * 5. 将线程池的指标对象注册为MBean
     *
     * 异常处理：
     * - 单个线程池注册失败时打印异常堆栈，但不中断整个导出过程
     * - 通过try-catch确保其他线程池的正常注册不受影响
     *
     * 线程安全：
     * - 方法内部无共享状态，可安全并发调用
     * - MBeanServer的注册操作本身是线程安全的
     *
     * @throws Exception 当获取MBeanServer失败时抛出异常
     * @see ThreadPoolManager#getAllPools()
     * @see javax.management.ObjectName
     * @see javax.management.MBeanServer#registerMBean(Object, ObjectName)
     */
    public static void export() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        ThreadPoolManager.getAllPools().forEach(pool -> {
            try {
                ObjectName name = new ObjectName("smart.pool:type=ThreadPool,name=" + pool.getMetrics().getPoolName());
                if (!mbs.isRegistered(name)) {
                    mbs.registerMBean(pool.getMetrics(), name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}