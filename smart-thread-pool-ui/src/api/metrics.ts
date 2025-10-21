import axios from 'axios';

export interface PoolMetrics {
    poolName: string;
    activeCount: number;
    corePoolSize: number;
    maximumPoolSize: number;
    completedTaskCount: number;
    taskCount: number;
    queueSize: number;
}

export async function fetchMetrics(): Promise<PoolMetrics[]> {
    const res = await axios.get<PoolMetrics[]>('/smart-pool/metrics');
    return res.data;
}
