import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/smart-pool': {
        target: 'http://localhost:8080', // Spring Boot 服务地址
        changeOrigin: true
      }
    }
  }
})
