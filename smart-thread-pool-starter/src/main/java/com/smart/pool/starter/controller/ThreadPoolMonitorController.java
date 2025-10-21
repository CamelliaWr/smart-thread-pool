package com.smart.pool.starter.controller;

import com.smart.pool.core.DynamicThreadPoolExecutor;
import com.smart.pool.core.ThreadPoolManager;
import com.smart.pool.core.metrics.PoolMetrics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 线程池监控控制器
 *
 * 功能说明：
 * 提供RESTful API接口，用于查询智能线程池的运行指标和状态信息
 * 支持通过HTTP GET请求获取所有线程池的实时指标数据
 *
 * 设计特点：
 * 1. 采用@RestController注解，自动将返回值序列化为JSON格式
 * 2. 统一的基础路径"/smart-pool"，便于API版本管理和聚合
 * 3. 无状态设计，不保存任何实例状态，支持多实例部署
 * 4. 轻量级实现，直接委托给ThreadPoolManager获取数据
 *
 * API规范：
 * - 基础路径：/smart-pool
 * - 指标接口：GET /smart-pool/metrics
 * - 返回格式：JSON数组，包含所有线程池的PoolMetrics对象
 * - 响应状态：200 OK（成功）
 *
 * 使用场景：
 * - 前端监控系统实时展示线程池状态
 * - 运维人员通过API获取线程池指标
 * - 集成到现有的监控告警平台
 * - 调试和诊断线程池相关问题
 *
 * 性能考虑：
 * - 直接委托调用，无额外业务逻辑处理
 * - 使用Java Stream API进行高效的数据转换
 * - 不缓存数据，确保返回最新的指标信息
 *
 * 集成说明：
 * 该控制器由Spring Boot自动扫描并注册，无需额外配置
 * 依赖ThreadPoolManager获取线程池实例，依赖PoolMetrics提供指标数据
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 * @see ThreadPoolManager#getAllPools()
 * @see DynamicThreadPoolExecutor#getMetrics()
 * @see PoolMetrics
 */
@RestController
@RequestMapping("/smart-pool")
public class ThreadPoolMonitorController {

    /**
     * 获取所有线程池指标
     *
     * 功能描述：
     * 查询当前应用中所有智能线程池的运行指标，包括活跃线程数、队列大小、
     * 已完成任务数、总任务数等关键性能指标
     *
     * 实现逻辑：
     * 1. 通过ThreadPoolManager获取所有注册的线程池实例
     * 2. 使用Stream API将线程池实例转换为对应的指标对象
     * 3. 收集所有指标并返回List集合
     *
     * 数据格式：
     * 返回PoolMetrics对象列表，每个对象包含单个线程池的完整指标信息：
     * - poolName：线程池名称
     * - activeCount：当前活跃线程数
     * - queueSize：队列中等待的任务数
     * - completedTaskCount：已完成的任务总数
     * - totalTaskCount：总任务数（已完成+进行中）
     *
     * 异常处理：
     * - 正常情况下返回200状态码
     * - 如ThreadPoolManager异常，由Spring全局异常处理机制处理
     * - 空线程池情况返回空列表（[]），而非null
     *
     * 调用示例：
     * <pre>
     * GET http://localhost:8080/smart-pool/metrics
     *
     * 响应示例：
     * [
     *   {
     *     "poolName": "order-pool",
     *     "activeCount": 4,
     *     "queueSize": 12,
     *     "completedTaskCount": 1250,
     *     "totalTaskCount": 1266
     *   }
     * ]
     * </pre>
     *
     * 性能说明：
     * - 时间复杂度：O(n)，n为线程池数量
     * - 空间复杂度：O(n)，存储所有线程池指标
     * - 无缓存机制，每次调用都获取最新数据
     *
     * @return 包含所有线程池指标的列表，不会返回null
     */
    @GetMapping("/metrics")
    public List<PoolMetrics> metrics() {
        return ThreadPoolManager.getAllPools()
                .stream()
                .map(DynamicThreadPoolExecutor::getMetrics)
                .collect(Collectors.toList());
    }
}