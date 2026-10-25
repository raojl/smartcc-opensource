import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      // 后端 API 代理
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/actuator': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  }
})
