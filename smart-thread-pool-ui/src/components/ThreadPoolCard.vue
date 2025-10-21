<template>
  <el-card class="box-card" style="margin-bottom:16px">
    <div slot="header">{{ pool.poolName }}</div>
    <el-row :gutter="20">
      <el-col :span="6">
        <div>活跃线程数: {{ pool.activeCount }}</div>
      </el-col>
      <el-col :span="6">
        <div>核心线程数: {{ pool.corePoolSize }}</div>
      </el-col>
      <el-col :span="6">
        <div>最大线程数: {{ pool.maximumPoolSize }}</div>
      </el-col>
      <el-col :span="6">
        <div>队列大小: {{ pool.queueSize }}</div>
      </el-col>
      <el-col :span="6" style="margin-top:8px;">
        <div>已完成任务数: {{ pool.completedTaskCount }}</div>
      </el-col>
      <el-col :span="6" style="margin-top:8px;">
        <div>总任务数: {{ pool.taskCount }}</div>
      </el-col>
    </el-row>
  </el-card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { PoolMetrics } from '../api/metrics';

export default defineComponent({
  name: 'ThreadPoolCard',
  props: {
    pool: {
      type: Object as PropType<PoolMetrics>,
      required: true,
    },
  },
});
</script>
