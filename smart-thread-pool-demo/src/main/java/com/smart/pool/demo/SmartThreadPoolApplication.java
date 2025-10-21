package com.smart.pool.demo;

import com.smart.pool.monitor.MetricsCollector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.smart.pool")
@EnableScheduling
public class SmartThreadPoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartThreadPoolApplication.class, args);

        // 启动指标收集器（Prometheus + JMX）
        MetricsCollector.startCollecting();
    }
}
