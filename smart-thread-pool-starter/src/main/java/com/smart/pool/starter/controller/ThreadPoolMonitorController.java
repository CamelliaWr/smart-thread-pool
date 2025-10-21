package com.smart.pool.starter.controller;

import com.smart.pool.core.DynamicThreadPoolExecutor;
import com.smart.pool.core.ThreadPoolManager;
import com.smart.pool.core.metrics.PoolMetrics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/smart-pool")
public class ThreadPoolMonitorController {

    @GetMapping("/metrics")
    public List<PoolMetrics> metrics() {
        return ThreadPoolManager.getAllPools()
                .stream()
                .map(DynamicThreadPoolExecutor::getMetrics)
                .collect(Collectors.toList());
    }
}
