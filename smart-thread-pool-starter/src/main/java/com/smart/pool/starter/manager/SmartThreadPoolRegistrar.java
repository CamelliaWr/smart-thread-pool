package com.smart.pool.starter.manager;

import com.smart.pool.core.DynamicThreadPoolExecutor;
import com.smart.pool.core.ThreadPoolManager;
import com.smart.pool.core.strategy.AdaptiveStrategy;
import com.smart.pool.starter.annotation.SmartPool;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 智能线程池注册器
 *
 * 功能说明：
 * Spring Bean后置处理器，负责处理@SmartPool注解，自动创建并注入DynamicThreadPoolExecutor实例
 * 实现依赖注入的自动化，简化线程池的使用和管理
 *
 * 设计特点：
 * 1. 实现BeanPostProcessor接口，在Spring Bean初始化阶段进行字段注入
 * 2. 支持@SmartPool注解的解析和参数提取
 * 3. 自动线程池命名策略（注解名优先，字段名备选）
 * 4. 集成ThreadPoolManager进行统一注册管理
 * 5. 延迟加载AdaptiveStrategy策略，避免循环依赖
 *
 * 工作流程：
 * 1. 扫描Spring容器中所有Bean的字段
 * 2. 识别带有@SmartPool注解的字段
 * 3. 根据注解参数创建DynamicThreadPoolExecutor实例
 * 4. 注册到ThreadPoolManager进行统一管理
 * 5. 通过反射将线程池实例注入到目标字段
 *
 * 线程池配置：
 * - 存活时间：60秒（空闲线程回收时间）
 * - 队列类型：LinkedBlockingQueue（无界队列）
 * - 线程工厂：Executors.defaultThreadFactory()
 * - 拒绝策略：ThreadPoolExecutor.AbortPolicy()
 *
 * 异常处理：
 * - 反射访问异常转换为RuntimeException抛出
 * - 确保Spring容器启动失败时提供明确的错误信息
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 * @see BeanPostProcessor
 * @see SmartPool
 * @see DynamicThreadPoolExecutor
 * @see ThreadPoolManager
 */
@Component
public class SmartThreadPoolRegistrar implements BeanPostProcessor {

    /**
     * 自适应策略管理器
     *
     * 设计考虑：
     * - 使用@Lazy注解避免循环依赖问题
     * - 在构造函数中注入，确保依赖可用
     * - 虽然当前实现中未直接使用，但为未来扩展预留
     *
     * 潜在用途：
     * - 线程池创建后的策略初始化
     * - 运行时参数的动态调整
     * - 性能监控和自适应优化
     */
    private final AdaptiveStrategy strategy;

    /**
     * 构造函数
     *
     * 依赖注入：
     * 通过Spring容器自动注入AdaptiveStrategy实例，采用延迟加载策略
     *
     * @param strategy 自适应策略管理器，用于线程池的智能调优
     */
    public SmartThreadPoolRegistrar(@Lazy AdaptiveStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Bean初始化前置处理器
     *
     * 核心功能：
     * 在Spring Bean初始化之前，扫描并处理@SmartPool注解字段，实现线程池的自动注入
     *
     * 处理流程：
     * 1. 获取Bean类的所有声明字段（包括私有字段）
     * 2. 遍历字段，识别带有@SmartPool注解的字段
     * 3. 提取注解参数，确定线程池名称（注解name优先，字段名备选）
     * 4. 创建DynamicThreadPoolExecutor实例，配置核心参数
     * 5. 注册到ThreadPoolManager进行生命周期管理
     * 6. 通过反射设置字段可访问性并注入线程池实例
     *
     * 线程池配置详解：
     * - poolName：线程池唯一标识，用于监控和管理
     * - corePoolSize：核心线程数，来自@SmartPool注解
     * - maxPoolSize：最大线程数，来自@SmartPool注解
     * - keepAliveTime：60秒，空闲线程回收时间
     * - workQueue：LinkedBlockingQueue，无界队列设计
     * - threadFactory：默认线程工厂，标准化线程创建
     * - handler：AbortPolicy，任务拒绝时抛出异常
     *
     * 命名策略：
     * if (annotation.name().isEmpty()) {
     *     poolName = field.getName(); // 使用字段名作为备选
     * } else {
     *     poolName = annotation.name(); // 优先使用注解指定的名称
     * }
     *
     * 异常处理：
     * - IllegalAccessException：转换为RuntimeException，确保Spring感知启动失败
     * - 提供明确的异常信息，便于问题定位和修复
     *
     * 线程安全：
     * - 方法无状态，可安全并发处理多个Bean
     * - 每个Bean的字段处理相互独立，无共享状态竞争
     *
     * @param bean Spring容器中的Bean实例
     * @param beanName Bean在容器中的名称
     * @return 处理后的Bean实例（可能被修改）
     * @throws BeansException 当字段注入失败时抛出
     *
     * 使用限制：
     * - 仅处理声明字段（getDeclaredFields），不处理继承字段
     * - 需要字段设置可访问性（setAccessible(true)）
     * - 每个@SmartPool字段都会创建独立的线程池实例
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(SmartPool.class)) {
                SmartPool annotation = field.getAnnotation(SmartPool.class);

                String poolName = annotation.name().isEmpty() ? field.getName() : annotation.name();

                DynamicThreadPoolExecutor executor = new DynamicThreadPoolExecutor(
                        poolName,
                        annotation.corePoolSize(),
                        annotation.maxPoolSize(),
                        60,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy()
                );

                ThreadPoolManager.register(poolName, executor);

                field.setAccessible(true);
                try {
                    field.set(bean, executor);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }
}