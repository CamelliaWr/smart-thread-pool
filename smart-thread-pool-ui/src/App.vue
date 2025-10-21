<template>
  <el-container style="height: 100vh;">
    <el-header style="color: white; font-size: 20px; background-color:#409EFF;">
      Smart Thread Pool Monitor
    </el-header>
    <el-main style="padding: 24px;">
      <Dashboard />
    </el-main>
  </el-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dashboard from './views/Dashboard.vue'

export default defineComponent({
  name: 'App',
  components: { Dashboard }
})
</script>
