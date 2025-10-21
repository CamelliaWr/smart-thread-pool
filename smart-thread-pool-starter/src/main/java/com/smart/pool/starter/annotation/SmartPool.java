package com.smart.pool.starter.annotation;

import java.lang.annotation.*;

/**
 * 智能线程池注入注解
 *
 * 功能说明：
 * 用于在Spring管理的Bean中自动注入DynamicThreadPoolExecutor实例的字段级注解
 * 支持通过注解参数自定义线程池的配置参数
 *
 * 设计特点：
 * 1. 字段级注解（@Target(ElementType.FIELD)），只能应用于字段声明
 * 2. 运行时保留（@Retention(RetentionPolicy.RUNTIME)），支持反射读取
 * 3. 文档化注解（@Documented），会出现在生成的Javadoc中
 * 4. 提供合理的默认值，简化基本使用场景
 *
 * 使用场景：
 * - 在Spring Boot应用中需要线程池的Service类中标注字段
 * - 替代手动创建和配置线程池的复杂过程
 * - 支持通过注解参数快速调整线程池行为
 *
 * 集成方式：
 * 与Spring的依赖注入机制集成，通过SmartPoolAnnotationBeanPostProcessor处理注解
 * 自动创建、配置并注入DynamicThreadPoolExecutor实例
 *
 * 配置示例：
 * <pre>
 * &#64;Service
 * public class OrderService {
 *     // 使用默认配置（core=4, max=8, name="")
 *     &#64;SmartPool
 *     private DynamicThreadPoolExecutor orderExecutor;
 *
 *     // 自定义配置
 *     &#64;SmartPool(name = "payment-pool", corePoolSize = 8, maxPoolSize = 16)
 *     private DynamicThreadPoolExecutor paymentExecutor;
 * }
 * </pre>
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 * @see com.smart.pool.core.DynamicThreadPoolExecutor
 * @see com.smart.pool.starter.processor.SmartPoolAnnotationBeanPostProcessor
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SmartPool {

    /**
     * 线程池名称
     *
     * 作用说明：
     * 为注入的线程池指定一个唯一的名称，用于标识和监控
     *
     * 命名规范：
     * - 建议使用小写字母和连字符（kebab-case）
     * - 保持名称简洁且具有描述性
     * - 避免使用特殊字符和空格
     *
     * 默认值：
     * 当名称为空字符串时，处理器会根据字段名自动生成线程池名称
     * 例如：字段名"orderExecutor" → 线程池名称"order-executor"
     *
     * 使用建议：
     * - 在监控和日志中用于区分不同线程池
     * - 建议为业务相关的线程池指定有意义的名称
     *
     * @return 线程池名称，默认为空字符串（自动生成）
     */
    String name() default "";

    /**
     * 核心线程数
     *
     * 参数说明：
     * 线程池中保持存活的最小线程数量，即使这些线程处于空闲状态
     *
     * 默认值：
     * 4个核心线程，适合大多数中小型应用场景
     *
     * 配置建议：
     * - CPU密集型任务：建议设置为CPU核心数或核心数+1
     * - IO密集型任务：可以设置更大的值，如CPU核心数×2
     * - 混合任务：根据实际压测结果调整
     *
     * 取值范围：
     * 必须为正整数，建议根据实际业务负载进行调优
     *
     * @return 核心线程数，默认为4
     */
    int corePoolSize() default 4;

    /**
     * 最大线程数
     *
     * 参数说明：
     * 线程池中允许的最大线程数量，当队列满时会创建新线程直到达到此上限
     *
     * 默认值：
     * 8个最大线程，为核心线程数的两倍，提供一定的弹性扩展能力
     *
     * 配置原则：
     * - maxPoolSize必须大于或等于corePoolSize
     * - 设置过大可能导致系统资源耗尽
     * - 设置过小可能无法充分利用系统资源
     *
     * 性能考虑：
     * - 需要结合系统资源和任务特性进行配置
     * - 建议通过压测确定最优值
     * - 配合队列容量和拒绝策略使用
     *
     * @return 最大线程数，默认为8
     */
    int maxPoolSize() default 8;
}