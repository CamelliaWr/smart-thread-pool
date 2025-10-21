package com.smart.pool.core.reject;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * SPI 接口：自定义拒绝策略
 */
public interface RejectedExecutionStrategy extends RejectedExecutionHandler {
}
