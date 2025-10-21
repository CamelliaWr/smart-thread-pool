<template>
  <div ref="chartRef" style="width:100%; height:400px;"></div>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { PoolMetrics } from '../api/metrics';

export default defineComponent({
  name: 'ThreadPoolChart',
  props: {
    pools: {
      type: Array as () => PoolMetrics[],
      required: true,
    },
  },
  setup(props) {
    const chartRef = ref<HTMLDivElement>();
    let chart: echarts.ECharts;
    const historyMap = new Map<
        string,
        { active: number[]; queue: number[]; tasks: number[] }
    >();

    const updateChart = () => {
      if (!chartRef.value) return;
      if (!chart) chart = echarts.init(chartRef.value);

      props.pools.forEach((pool) => {
        if (!historyMap.has(pool.poolName)) {
          historyMap.set(pool.poolName, { active: [], queue: [], tasks: [] });
        }
        const hist = historyMap.get(pool.poolName)!;
        hist.active.push(pool.activeCount);
        hist.queue.push(pool.queueSize);
        hist.tasks.push(pool.taskCount);
        if (hist.active.length > 20) hist.active.shift();
        if (hist.queue.length > 20) hist.queue.shift();
        if (hist.tasks.length > 20) hist.tasks.shift();
      });

      const series: any[] = [];
      const xAxisData = Array.from({ length: 20 }, (_, i) => i + 1);

      props.pools.forEach((pool) => {
        const hist = historyMap.get(pool.poolName)!;
        series.push(
            { name: `${pool.poolName} 活跃线程`, type: 'line', data: hist.active, smooth: true },
            { name: `${pool.poolName} 队列大小`, type: 'line', data: hist.queue, smooth: true },
            { name: `${pool.poolName} 总任务数`, type: 'line', data: hist.tasks, smooth: true }
        );
      });

      chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: series.map((s) => s.name) },
        xAxis: { type: 'category', data: xAxisData },
        yAxis: { type: 'value' },
        series,
      });
    };

    watch(() => props.pools, updateChart, { deep: true, immediate: true });

    return { chartRef };
  },
});
</script>
