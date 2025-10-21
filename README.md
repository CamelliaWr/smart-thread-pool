# Smart ThreadPool

Smart ThreadPool 是一个可动态调节、可监控的线程池框架，支持以下特性：

* **动态线程池**：根据任务队列情况自动调整线程数。
* **SPI 插件**：支持自定义线程池调节策略和拒绝策略。
* **任务优先级**：支持优先级任务执行。
* **依赖通知**：线程池之间可设置依赖关系，任务完成后通知其他线程池。
* **监控指标**：集成 Prometheus 和 JMX 指标收集。

---

## 模块结构

### 核心模块 `com.smart.pool.core`

* **DynamicThreadPoolExecutor**：自定义线程池实现，支持动态调整线程数并收集指标。
* **SmartPoolBuilder**：线程池构建器，简化线程池创建流程。
* **PriorityTask**：支持任务优先级的 Runnable 封装。
* **ThreadPoolManager**：管理所有线程池实例。
* **ThreadPoolDependencyManager**：管理线程池依赖关系。
* **MetricsCollector**：统一收集所有线程池指标并应用 SPI 调整策略。

### 拒绝策略 `com.smart.pool.core.reject`

* **RejectedExecutionStrategy**：自定义拒绝策略 SPI 接口。
* **DefaultRejectedStrategy**：默认拒绝策略，任务失败时重试 3 次。
* **RejectedStrategyManager**：SPI 管理器，按优先级选择拒绝策略执行。

### 调节策略 `com.smart.pool.core.strategy`

* **AdaptiveStrategy**：线程池调节策略 SPI 接口。
* **DefaultAdaptiveStrategy**：默认策略，根据队列大小调整核心线程数。
* **AdaptiveStrategyManager**：SPI 管理器，按优先级组合执行策略。

### 监控模块 `com.smart.pool.monitor`

* **PrometheusExporter**：Prometheus 指标收集与 HTTP 导出。
* **JmxMetricsExporter**：JMX MBean 注册。
* **MetricsCollector**：定时刷新指标并应用策略。

### 示例模块 `com.smart.pool.demo`

* **SmartThreadPoolApplication**：Spring Boot 示例启动类。
* **OrderService**：示例服务，展示线程池的自动注入与任务提交。

### Starter 模块 `com.smart.pool.starter`

* **@SmartPool 注解**：简化线程池注入。
* **SmartThreadPoolRegistrar**：Spring BeanPostProcessor，自动注册并注入线程池。
* **ThreadPoolMonitorController**：提供 HTTP 接口 `/smart-pool/metrics` 查看线程池指标。

---

## 快速开始

### 1. Maven 依赖

```xml
<dependency>
    <groupId>com.smart.pool</groupId>
    <artifactId>smart-threadpool</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 注入线程池

```java
@Service
public class OrderService {

    @SmartPool(name = "order-pool", corePoolSize = 4, maxPoolSize = 8)
    private DynamicThreadPoolExecutor orderExecutor;

    public void processOrder(Runnable task) {
        orderExecutor.submit(task);
    }
}
```

### 3. 构建自定义线程池

```java
DynamicThreadPoolExecutor executor = SmartPoolBuilder.create("custom-pool")
        .corePoolSize(4)
        .maxPoolSize(8)
        .queueCapacity(200)
        .build();

executor.submit(() -> {
    System.out.println("Task executed in custom pool");
});
```

### 4. 设置线程池依赖

```java
ThreadPoolDependencyManager.addDependency("order-pool", "payment-pool");
```

### 5. 查看监控指标

* HTTP 接口: `GET /smart-pool/metrics`
* Prometheus: 默认端口 `9090`
* JMX: `smart.pool:type=ThreadPool,name=<poolName>`

---

## 自定义扩展

### 自定义调节策略

```java
public class MyAdaptiveStrategy implements AdaptiveStrategy {
    @Override
    public void adjust(DynamicThreadPoolExecutor executor) {
        if (executor.getQueue().size() > 50) {
            executor.setCorePoolSize(executor.getCorePoolSize() + 1);
        }
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
```

### 自定义拒绝策略

```java
public class MyRejectedStrategy implements RejectedExecutionStrategy {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        System.err.println("Task rejected: " + r);
    }
}
```

