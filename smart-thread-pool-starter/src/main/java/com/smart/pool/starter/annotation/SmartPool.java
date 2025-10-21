package com.smart.pool.starter.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SmartPool {
    String name() default "";
    int corePoolSize() default 4;
    int maxPoolSize() default 8;
}
