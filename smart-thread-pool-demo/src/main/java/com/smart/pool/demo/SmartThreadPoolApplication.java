package com.smart.pool.demo;

import com.smart.pool.monitor.MetricsCollector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智能线程池演示应用主类
 *
 * 功能说明：
 * 1. Spring Boot应用启动入口，配置组件扫描范围为com.smart.pool包及其子包
 * 2. 启用Spring定时任务调度功能
 * 3. 启动指标收集器，支持Prometheus和JMX监控
 *
 * 设计特点：
 * - 采用@SpringBootApplication注解简化配置
 * - 通过scanBasePackages指定组件扫描路径，确保智能线程池相关组件被正确加载
 * - 集成@EnableScheduling启用定时任务支持
 * - 应用启动后自动初始化指标收集系统
 *
 * 使用场景：
 * 作为智能线程池框架的演示应用，展示如何通过注解方式集成和使用智能线程池
 *
 * @author Smart Thread Pool
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.smart.pool")
@EnableScheduling
public class SmartThreadPoolApplication {

    /**
     * 应用主入口方法
     *
     * 执行流程：
     * 1. 启动Spring Boot应用上下文，加载所有配置和组件
     * 2. 初始化完成后启动指标收集器，开始收集线程池运行指标
     *
     * 指标收集功能：
     * - Prometheus格式指标：可通过HTTP端点暴露，供Prometheus服务器抓取
     * - JMX指标：通过JMX MBean暴露，支持JConsole等工具监控
     *
     * @param args 命令行参数，当前应用未使用特定参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SmartThreadPoolApplication.class, args);

        // 启动指标收集器（Prometheus + JMX）
        MetricsCollector.startCollecting();
    }
}
