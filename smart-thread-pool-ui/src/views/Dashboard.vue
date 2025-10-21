<template>
  <div>
    <div v-for="pool in metrics" :key="pool.poolName">
      <ThreadPoolCard :pool="pool" />
    </div>
    <ThreadPoolChart :pools="filteredPools" />
    <ThreadPoolRealtimePanel :onSelectPool="handlePoolSelect" />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed } from 'vue';
import { fetchMetrics, PoolMetrics } from '../api/metrics';
import ThreadPoolCard from '../components/ThreadPoolCard.vue';
import ThreadPoolChart from '../components/ThreadPoolChart.vue';
import ThreadPoolRealtimePanel from '../components/ThreadPoolRealtimePanel.vue';

export default defineComponent({
  name: 'Dashboard',
  components: { ThreadPoolCard, ThreadPoolChart, ThreadPoolRealtimePanel },
  setup() {
    const metrics = ref<PoolMetrics[]>([]);
    const selectedPool = ref<string | null>(null);

    const loadMetrics = async () => {
      const data = await fetchMetrics();
      metrics.value = data;
    };

    const handlePoolSelect = (poolName: string | null) => {
      selectedPool.value = poolName;
    };

    const filteredPools = computed(() =>
        selectedPool.value
            ? metrics.value.filter((pool) => pool.poolName === selectedPool.value)
            : metrics.value
    );

    onMounted(() => {
      loadMetrics();
      setInterval(loadMetrics, 5000);
    });

    return { metrics, filteredPools, handlePoolSelect };
  },
});
</script>
