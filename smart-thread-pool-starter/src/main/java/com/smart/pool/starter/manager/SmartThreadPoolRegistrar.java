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

@Component
public class SmartThreadPoolRegistrar implements BeanPostProcessor {

    private final AdaptiveStrategy strategy;

    public SmartThreadPoolRegistrar(@Lazy AdaptiveStrategy strategy) {
        this.strategy = strategy;
    }

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
