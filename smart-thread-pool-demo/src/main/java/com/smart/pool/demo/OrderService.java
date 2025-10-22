package com.smart.pool.demo;

import com.smart.pool.core.DynamicThreadPoolExecutor;
import com.smart.pool.starter.annotation.SmartPool;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 智能订单服务示例
 *
 * 功能说明：
 * 演示智能线程池框架的核心特性，包括动态扩缩容、负载监控、依赖管理和生命周期模拟。
 * 通过模拟真实的订单处理场景，展示线程池如何根据负载自动调节参数。
 *
 * 设计特点：
 * 1. 多线程池协作：订单池和支付池形成处理链路
 * 2. 智能扩缩容：根据负载阈值自动调整核心线程数
 * 3. 生命周期监控：实时输出线程池状态变化
 * 4. 依赖关系管理：支付池依赖订单池的执行结果
 *
 * 线程池配置：
 * - order-pool：核心4→最大12，激进扩容策略，负载驱动优先级
 * - payment-pool：核心2→最大6，平滑扩容策略，依赖order-pool
 *
 * 使用场景：
 * 适用于电商平台的订单处理系统，可处理高并发的订单创建和支付流程。
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 * @see DynamicThreadPoolExecutor
 * @see SmartPool
 */
@Service
public class OrderService {

    /**
     * 订单处理线程池（核心业务）
     *
     * 配置详解：
     * - name：线程池唯一标识，用于监控和依赖管理
     * - corePoolSize：初始核心线程数，低负载时保持的最小线程
     * - maxPoolSize：最大线程数，高负载时的扩容上限
     * - scalingPolicy：扩容策略，AGGRESSIVE表示快速响应负载变化
     * - priorityStrategy：优先级策略，LOAD_BASED根据负载动态调整任务优先级
     * - enableLifecycleSimulation：启用生命周期监控，输出线程创建/销毁日志
     * - adjustIntervalSeconds：调节检查间隔，每5秒评估一次负载状态
     * - highLoadThreshold：高负载阈值80%，超过此值触发扩容
     * - lowLoadThreshold：低负载阈值20%，低于此值触发缩容
     *
     * 行为特征：
     * 1. 负载>80%时：核心线程数+1（最大不超过maxPoolSize）
     * 2. 负载<20%时：核心线程数-1（最小不低于corePoolSize）
     * 3. 每5秒检查一次，实时调整以适应当前负载
     */
    @SmartPool(
            name = "order-pool",
            corePoolSize = 4,
            maxPoolSize = 12,
            scalingPolicy = "AGGRESSIVE",
            priorityStrategy = "LOAD_BASED",
            enableLifecycleSimulation = true,
            adjustIntervalSeconds = 5,
            highLoadThreshold = 0.8,
            lowLoadThreshold = 0.2
    )
    private DynamicThreadPoolExecutor orderExecutor;

    /**
     * 支付处理线程池（依赖服务）
     *
     * 配置详解：
     * - name：支付线程池标识，用于监控和链路追踪
     * - corePoolSize：初始2个线程，处理基础支付请求
     * - maxPoolSize：最大6个线程，应对支付高峰期
     * - scalingPolicy：SMOOTH平滑扩容，避免支付服务抖动
     * - dependsOn：声明依赖order-pool，确保订单先创建再支付
     *
     * 依赖关系：
     * 支付池的处理能力会根据订单池的负载进行调节，形成完整的订单处理链路。
     * 当订单池高负载时，支付池也会相应扩容以处理后续的支付请求。
     */
    @SmartPool(
            name = "payment-pool",
            corePoolSize = 2,
            maxPoolSize = 6,
            scalingPolicy = "SMOOTH",
            dependsOn = {"order-pool"}
    )
    private DynamicThreadPoolExecutor paymentExecutor;

    /**
     * 服务初始化方法
     *
     * 启动时机：Spring容器创建OrderService Bean后自动调用
     * 主要功能：
     * 1. 启动订单生成器：模拟持续的用户下单请求
     * 2. 启动智能监控器：实时展示线程池状态和调节过程
     *
     * 设计考虑：
     * - 使用守护线程避免阻塞应用退出
     * - 无限循环模拟真实的持续业务流量
     * - 异常捕获确保模拟器持续运行
     */
    @PostConstruct
    public void init() {
        // 订单生成器线程 - 模拟真实业务场景
        Thread generator = new Thread(() -> {
            while (true) {
                processOrder(() -> {
                    try {
                        // 模拟订单处理时间：300-1500毫秒随机延迟
                        // 模拟不同订单的复杂程度和处理耗时
                        TimeUnit.MILLISECONDS.sleep(300 + (int) (Math.random() * 1200));

                        // 订单处理完成后触发支付流程
                        // 形成完整的订单->支付业务链路
                        paymentExecutor.submit(() -> simulatePayment());
                    } catch (InterruptedException ignored) {
                        // 中断异常忽略，保持生成器持续运行
                    }
                });

                // 每200毫秒产生一个新订单
                // 模拟高并发场景下的持续流量
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                }
            }
        }, "order-generator");

        generator.setDaemon(true);  // 守护线程，不阻止JVM退出
        generator.start();

        // 启动智能行为监控和模拟
        startSmartSimulation();
    }

    /**
     * 处理订单任务
     *
     * 线程安全：由orderExecutor保证线程安全执行
     * 异常处理：任务内部异常由线程池的异常处理器处理
     *
     * @param task 订单处理任务，包含具体的订单业务逻辑
     */
    public void processOrder(Runnable task) {
        orderExecutor.submit(task);
    }

    /**
     * 模拟支付处理
     *
     * 处理逻辑：
     * - 模拟支付网关调用延迟：500-1500毫秒
     * - 模拟不同支付方式的耗时差异
     * - 包含网络延迟、银行处理时间等因素
     *
     * 设计考虑：
     * 支付作为敏感操作，需要更长的处理时间和更稳定的性能。
     */
    private void simulatePayment() {
        try {
            TimeUnit.MILLISECONDS.sleep(500 + (int) (Math.random() * 1000));
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * 智能行为模拟器
     *
     * 核心功能：
     * 1. 实时监控order-pool的负载状态
     * 2. 模拟线程池的智能扩缩容行为
     * 3. 输出详细的调节日志用于演示和学习
     *
     * 监控指标：
     * - 活跃线程数：当前正在处理任务的线程数量
     * - 核心线程数：当前配置的核心线程数量
     * - 最大线程数：线程池允许的最大线程数量
     * - 负载比例：活跃线程/最大线程，反映当前繁忙程度
     *
     * 调节策略：
     * 1. 高负载（>80%）：增加核心线程数以提高处理能力
     * 2. 低负载（<20%）：减少核心线程数以节约资源
     * 3. 正常负载：保持当前配置，避免频繁调节
     *
     * 输出格式：
     * [Lifecycle] 线程池生命周期状态
     * [Adaptive] 自适应扩缩容决策
     * [Priority] 任务优先级调节信息
     */
    private void startSmartSimulation() {
        Thread simulator = new Thread(() -> {
            while (true) {
                try {
                    // 获取当前线程池状态
                    int active = orderExecutor.getActiveCount();      // 活跃线程
                    int core = orderExecutor.getCorePoolSize();        // 核心线程
                    int max = orderExecutor.getMaximumPoolSize();      // 最大线程

                    // 计算负载比例
                    double load = (double) active / max;

                    // 生命周期监控输出（30%概率，避免日志过多）
                    if (Math.random() < 0.3) {
                        System.out.printf("[Lifecycle] [order-pool] 活跃线程: %d/%d%n",
                                active, max);
                    }

                    // 高负载扩容逻辑
                    if (load > 0.8 && core < max) {
                        System.out.printf("[Adaptive] 负载%.2f，高负载扩容 -> %d → %d%n",
                                load, core, Math.min(max, core + 1));
                        orderExecutor.setCorePoolSize(core + 1);
                    }
                    // 低负载缩容逻辑
                    else if (load < 0.2 && core > 2) {
                        System.out.printf("[Adaptive] 负载%.2f，低负载收缩 -> %d → %d%n",
                                load, core, core - 1);
                        orderExecutor.setCorePoolSize(core - 1);
                    }

                    // 优先级策略监控输出
                    if ("LOAD_BASED".equalsIgnoreCase("LOAD_BASED")) {
                        System.out.printf("[Priority] 根据负载 %.2f 动态调整任务优先级%n",
                                load);
                    }

                    // 每5秒检查一次，避免过于频繁的调节
                    Thread.sleep(5000);
                } catch (Exception ignored) {
                    // 忽略异常，保持监控器持续运行
                }
            }
        }, "smart-simulator");

        simulator.setDaemon(true);  // 守护线程
        simulator.start();
    }
}