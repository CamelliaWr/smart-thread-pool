# Smart Thread Pool

高性能、可扩展的 Java 动态线程池框架，支持：

* **动态调节线程池大小**（内置默认策略 + SPI 插件自定义策略）
* **统一指标采集**（Prometheus / JMX / REST）
* **注解/Builder 创建线程池**，轻松集成 Spring Boot
* **完全隔离线程池参数**，支持多池共存

---

## 模块结构

```
smart-thread-pool/                    
│
├── smart-thread-pool-core/           # 核心线程池模块
│   ├── DynamicThreadPoolExecutor.java
│   ├── ThreadPoolManager.java
│   ├── SmartPoolBuilder.java
│   └── strategy/
│       ├── AdaptiveStrategy.java
│       └── DefaultAdaptiveStrategy.java
│   └── metrics/
│       └── PoolMetrics.java
│
├── smart-thread-pool-monitor/        # 监控模块
│   ├── MetricsCollector.java
│   ├── PrometheusExporter.java
│   └── JmxMetricsExporter.java
│
├── smart-thread-pool-starter/        # 注解 + BeanPostProcessor 模块
│   ├── annotation/SmartPool.java
│   ├── manager/SmartThreadPoolRegistrar.java
│   └── controller/ThreadPoolMonitorController.java
│
└── smart-thread-pool-demo/           # 示例业务模块
    ├── OrderService.java
    └── SmartThreadPoolApplication.java
```

---

## 快速开始

### 1️⃣ 添加依赖

```xml
<dependency>
    <groupId>com.smart</groupId>
    <artifactId>smart-thread-pool-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2️⃣ 使用 @SmartPool 注解创建线程池

```java
@Service
public class MyService {

    @SmartPool(name = "order-pool", corePoolSize = 4, maxPoolSize = 8)
    private DynamicThreadPoolExecutor orderExecutor;

    @PostConstruct
    public void init() {
        orderExecutor.submit(() -> System.out.println("线程池已启动"));
    }

    public void submitTask(Runnable task) {
        orderExecutor.submit(task);
    }
}
```

### 3️⃣ 查看线程池指标

#### REST 接口

```
GET http://localhost:8080/smart-pool/metrics
```

#### JMX

* 打开 JConsole
* 连接应用
* 找到 `smart.pool -> ThreadPool -> order-pool`
* 查看 `activeCount`, `corePoolSize`, `maximumPoolSize` 等指标

#### Prometheus

* 默认端口 `9090`
* 可抓取 `/metrics` 获取线程池指标

### 4️⃣ 自定义线程调节策略 (SPI 插件)

1. **实现 AdaptiveStrategy 接口**

```java
public class CustomAdaptiveStrategy implements AdaptiveStrategy {
    @Override
    public void adjustPool(DynamicThreadPoolExecutor executor) {
        if (executor.getQueue().size() > executor.getMaximumPoolSize() / 2) {
            executor.setCorePoolSize(Math.min(executor.getCorePoolSize() + 1, executor.getMaximumPoolSize()));
        }
    }
}
```

2. **创建 SPI 配置文件**

在 `src/main/resources/META-INF/services/com.smart.pool.core.strategy.AdaptiveStrategy` 中写：

```
com.smart.pool.core.strategy.DefaultAdaptiveStrategy
com.example.custom.CustomAdaptiveStrategy
```

3. 框架会自动加载策略，每 5 秒调用一次

### 5️⃣ 使用 SmartPoolBuilder 构建自定义线程池

```java
DynamicThreadPoolExecutor customPool = SmartPoolBuilder.create("custom-pool")
    .corePoolSize(32)
    .maxPoolSize(128)
    .queueCapacity(1000)
    .keepAliveTime(120)
    .allowCoreThreadTimeOut(true)
    .build();
```

### 6️⃣ 多线程池共存

* 支持任意数量的线程池
* 各线程池参数独立
* Prometheus / JMX / REST 指标按 `poolName` 区分

### 7️⃣ 注意事项

1. **SPI 插件机制**：开发者可自定义策略，无需修改框架
2. **指标刷新周期**：默认每 5 秒刷新
3. **Prometheus**：默认端口 9090，可在 `MetricsCollector` 中修改
4. **JMX**：遵循标准 MBean / MXBean 规范
5. **线程池参数隔离**：每个池独立，互不影响


> 通过以上方式，开发者可以**即插即用**，轻松创建、管理和监控线程池，同时可通过 SPI 插件灵活扩展调节策略。
