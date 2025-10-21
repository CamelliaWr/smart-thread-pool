package com.smart.pool.monitor;

import com.smart.pool.core.ThreadPoolManager;

import javax.management.*;
import java.lang.management.ManagementFactory;

public class JmxMetricsExporter {

    public static void export() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        ThreadPoolManager.getAllPools().forEach(pool -> {
            try {
                ObjectName name = new ObjectName("smart.pool:type=ThreadPool,name=" + pool.getMetrics().getPoolName());
                if (!mbs.isRegistered(name)) {
                    mbs.registerMBean(pool.getMetrics(), name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
