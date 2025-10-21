// src/api/prometheus.ts
import axios from 'axios';

export interface PoolMetrics {
    poolName: string;
    activeCount: number;
    corePoolSize: number;
    maximumPoolSize: number;
    queueSize: number;
    taskCount: number;
    completedTaskCount: number;
}

export async function fetchPrometheusMetrics(): Promise<PoolMetrics[]> {
    const res = await axios.get<PoolMetrics[]>('/smart-pool/metrics');
    return res.data;
}
