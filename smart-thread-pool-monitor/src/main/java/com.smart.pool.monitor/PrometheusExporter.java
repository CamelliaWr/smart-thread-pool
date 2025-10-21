package com.smart.pool.monitor;

import com.smart.pool.core.ThreadPoolManager;
import com.smart.pool.core.metrics.PoolMetrics;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;

public class PrometheusExporter {

    private static final Gauge activeThreads = Gauge.build()
            .name("threadpool_active_threads").help("Active threads").labelNames("pool").register();
    private static final Gauge queueSize = Gauge.build()
            .name("threadpool_queue_size").help("Queue size").labelNames("pool").register();

    private Thread monitorThread;

    public void start(int port) throws Exception {
        new HTTPServer(port);

        monitorThread = new Thread(() -> {
            while (true) {
                ThreadPoolManager.getAllPools().forEach(pool -> {
                    PoolMetrics metrics = pool.getMetrics();
                    activeThreads.labels(metrics.getPoolName()).set(metrics.getActiveCount());
                    queueSize.labels(metrics.getPoolName()).set(metrics.getQueueSize());
                });
                try { Thread.sleep(5000); } catch (Exception ignored) {}
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
}
