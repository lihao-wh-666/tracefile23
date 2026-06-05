import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 6000,
    proxy: {
      '/api': {
        target: 'http://localhost:6080',
        changeOrigin: true
      }
    }
  }
})
