package com.smart.pool.starter.annotation;

import java.lang.annotation.*;

/**
 * 智能线程池注入注解（增强版）
 *
 * 功能说明：
 * 通过注解直接声明并注入带有智能管理能力的DynamicThreadPoolExecutor。
 * 支持线程生命周期模拟、动态扩缩容、线程间依赖与优先级调节。
 *
 * 使用场景：
 * - 需要动态调整线程池大小的业务场景
 * - 对线程池性能有监控需求的系统
 * - 多线程池之间存在依赖关系的复杂业务
 *
 * 使用示例：
 * <pre>
 * public class OrderService {
 *     // 基础配置示例
 *     &#64;SmartPool(name = "order-pool", corePoolSize = 4, maxPoolSize = 8)
 *     private DynamicThreadPoolExecutor orderExecutor;
 *
 *     // 高级配置示例
 *     &#64;SmartPool(
 *         name = "payment-pool",
 *         corePoolSize = 2,
 *         maxPoolSize = 16,
 *         queueCapacity = 200,
 *         scalingPolicy = "AGGRESSIVE",
 *         highLoadThreshold = 0.8,
 *         lowLoadThreshold = 0.2,
 *         dependsOn = {"order-pool"}
 *     )
 *     private DynamicThreadPoolExecutor paymentExecutor;
 * }
 * </pre>
 *
 * 线程池依赖说明：
 * - 当依赖的线程池关闭时，当前线程池会自动暂停接收新任务
 * - 支持多个线程池之间的级联控制
 * - 依赖关系在运行时动态解析和维护
 *
 * 扩缩容策略说明：
 * - SMOOTH：平滑调整，避免突然的线程数变化
 * - AGGRESSIVE：快速响应负载变化，适合突发流量场景
 * - STABLE：固定大小，不进行自动调整
 *
 * 监控指标：
 * - 活跃线程数、队列大小、任务完成率
 * - 线程创建/销毁事件（当启用生命周期模拟时）
 * - 负载变化趋势和扩容触发记录
 *
 * 注意事项：
 * - 线程池名称必须唯一，否则会抛出异常
 * - 负载阈值的合理范围是0.1-0.9，避免设置极端值
 * - 建议根据实际业务负载测试来调整参数
 *
 * @author Smart
 * @since 2.0.0
 * @see com.smart.pool.core.DynamicThreadPoolExecutor
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SmartPool {

    // ===================== 基础配置 =====================

    /**
     * 线程池名称
     *
     * 必填项，用于唯一标识线程池实例。
     * 建议命名规范：{业务模块}-{功能}-{pool}，如：order-payment-pool
     *
     * @return 线程池名称
     */
    String name() default "";

    /**
     * 核心线程数
     *
     * 线程池保持的最小线程数量，即使这些线程处于空闲状态也不会被回收
     * 除非设置了allowCoreThreadTimeout=true。
     *
     * 建议设置：
     * - CPU密集型任务：CPU核心数 + 1
     * - IO密集型任务：CPU核心数 * 2
     *
     * @return 核心线程数，默认4
     */
    int corePoolSize() default 4;

    /**
     * 最大线程数
     *
     * 线程池允许创建的最大线程数量。
     * 当队列满时，线程池会创建新线程直到达到这个上限。
     *
     * 设置建议：
     * - 考虑系统资源限制和线程切换开销
     * - 通常设置为corePoolSize的2-4倍
     *
     * @return 最大线程数，默认8
     */
    int maxPoolSize() default 8;

    /**
     * 队列容量
     *
     * 任务队列的最大容量。当所有核心线程都在工作时，
     * 新任务会被放入队列等待执行。
     *
     * 队列类型：LinkedBlockingQueue
     * 建议设置：根据任务峰值和平均处理时间评估
     *
     * @return 队列容量，默认100
     */
    int queueCapacity() default 100;

    /**
     * 线程空闲存活时间（秒）
     *
     * 当线程空闲时间超过这个值时，多余的线程会被回收，
     * 直到线程数等于corePoolSize。
     *
     * 设置建议：
     * - 短时任务：30-60秒
     * - 长时任务：120-300秒
     *
     * @return 空闲存活时间，默认60秒
     */
    long keepAliveSeconds() default 60;

    /**
     * 是否允许核心线程超时回收
     *
     * 如果设置为true，核心线程在空闲keepAliveSeconds时间后也会被回收。
     * 适用于线程池使用频率较低的场景，可以节省系统资源。
     *
     * @return 是否允许核心线程超时，默认false
     */
    boolean allowCoreThreadTimeout() default false;

    // ===================== 智能控制 =====================

    /**
     * 是否启用生命周期模拟
     *
     * 启用后会定期输出线程创建、销毁等事件日志，
     * 便于调试和监控线程池的运行状态。
     *
     * 输出示例：
     * [Thread-1] INFO Thread created: order-pool-thread-1
     * [Thread-2] INFO Thread destroyed: order-pool-thread-2
     *
     * @return 是否启用生命周期模拟，默认true
     */
    boolean enableLifecycleSimulation() default true;

    /**
     * 是否允许自动回收空闲线程
     *
     * 当系统检测到线程长时间空闲时，自动回收这些线程以节省资源。
     * 与allowCoreThreadTimeout配合使用可以实现更精细的线程管理。
     *
     * @return 是否自动回收空闲线程，默认true
     */
    boolean autoRecycle() default true;

    /**
     * 动态优先级调整策略
     *
     * 根据选择的策略动态调整线程池中任务的执行优先级。
     *
     * 策略说明：
     * - "NONE"：不进行调整，保持默认优先级
     * - "LOAD_BASED"：根据系统负载自动调整，高负载时降低新任务优先级
     * - "DEADLINE_BASED"：根据任务的延迟情况调整，延迟高的任务优先级提升
     *
     * 应用场景：
     * - 混合负载系统建议使用LOAD_BASED
     * - 实时性要求高的系统建议使用DEADLINE_BASED
     *
     * @return 优先级策略，默认"LOAD_BASED"
     */
    String priorityStrategy() default "LOAD_BASED";

    /**
     * 线程池依赖配置
     *
     * 定义当前线程池依赖的其他线程池名称数组。
     * 当依赖的线程池不可用时，当前线程池会自动暂停。
     *
     * 使用场景：
     * - 业务流程中存在先后依赖关系
     * - 需要级联控制多个线程池的启停
     * - 防止下游线程池过载
     *
     * 示例：
     * dependsOn = {"order-pool", "inventory-pool"}
     * 表示当前线程池依赖于订单池和库存池
     *
     * @return 依赖的线程池名称数组，默认空数组
     */
    String[] dependsOn() default {};

    /**
     * 动态扩缩容策略
     *
     * 控制线程池如何根据负载情况动态调整线程数量。
     *
     * 策略对比：
     * - SMOOTH：渐进式调整，每次增减1个线程，适合稳定业务
     * - AGGRESSIVE：快速调整，可一次增减多个线程，适合突发流量
     * - STABLE：禁用自动调整，适合对线程数有严格要求的场景
     *
     * 触发条件：
     * - 高负载：活跃线程数/最大线程数 > highLoadThreshold
     * - 低负载：活跃线程数/最大线程数 < lowLoadThreshold
     *
     * @return 扩缩容策略，默认"SMOOTH"
     */
    String scalingPolicy() default "SMOOTH";

    /**
     * 动态调整周期（秒）
     *
     * 系统检查负载并决定是否进行扩缩容的时间间隔。
     * 仅当scalingPolicy不为"STABLE"时生效。
     *
     * 设置建议：
     * - 高并发场景：1-3秒，快速响应负载变化
     * - 一般业务：5-10秒，平衡响应速度和系统开销
     * - 低负载场景：10-30秒，减少不必要的检查
     *
     * @return 调整周期，默认5秒
     */
    int adjustIntervalSeconds() default 5;

    /**
     * 最大负载阈值（0~1）
     *
     * 触发扩容的负载上限。当活跃线程数占最大线程数的比例超过此值时，
     * 系统会尝试增加线程数（不超过maxPoolSize）。
     *
     * 合理范围：0.6-0.9
     * 设置建议：
     * - 对延迟敏感：0.6-0.7，提前扩容避免排队
     * - 一般业务：0.7-0.8，平衡资源利用和响应速度
     * - 资源紧张：0.8-0.9，充分利用现有资源
     *
     * @return 最大负载阈值，默认0.75
     */
    double highLoadThreshold() default 0.75;

    /**
     * 低负载阈值（0~1）
     *
     * 触发缩容的负载下限。当活跃线程数占最大线程数的比例低于此值时，
     * 系统会尝试减少线程数（不低于corePoolSize）。
     *
     * 合理范围：0.1-0.5
     * 设置建议：
     * - 与highLoadThreshold保持一定差距，避免频繁震荡
     * - 建议差距至少0.2以上
     * - 考虑业务流量的波动范围
     *
     * @return 低负载阈值，默认0.25
     */
    double lowLoadThreshold() default 0.25;
}