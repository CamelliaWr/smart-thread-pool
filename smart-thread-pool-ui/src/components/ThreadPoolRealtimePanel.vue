<template>
  <el-card class="box-card" style="margin-bottom:16px">
    <div slot="header">实时线程池指标</div>
    <el-row :gutter="20">
      <el-col
          v-for="metric in metrics"
          :key="metric.pool"
          :span="8"
          style="margin-bottom:10px"
      >
        <el-card
            :class="{ 'active-pool': metric.pool === selectedPool }"
            @click="selectPool(metric.pool)"
        >
          <div><strong>{{ metric.pool }}</strong></div>
          <div>活跃线程数: {{ metric.activeThreads }}</div>
          <div>队列大小: {{ metric.queueSize }}</div>
          <div>任务总数: {{ metric.taskCount }}</div>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, watch } from 'vue';
import { PrometheusMetric, fetchPrometheusMetrics } from '../api/prometheus';

export default defineComponent({
  name: 'ThreadPoolRealtimePanel',
  props: {
    onSelectPool: {
      type: Function as unknown as () => (poolName: string | null) => void,
      required: true
    }
  },
  setup(props) {
    const metrics = ref<PrometheusMetric[]>([]);
    const selectedPool = ref<string | null>(null);

    const loadMetrics = async () => {
      const data = await fetchPrometheusMetrics();
      metrics.value = data;
    };

    const selectPool = (pool: string) => {
      selectedPool.value = selectedPool.value === pool ? null : pool;
      props.onSelectPool(selectedPool.value);
    };

    onMounted(() => {
      loadMetrics();
      setInterval(loadMetrics, 3000);
    });

    return { metrics, selectedPool, selectPool };
  },
});
</script>

<style>
.active-pool {
  border: 2px solid #409eff;
}
</style>
