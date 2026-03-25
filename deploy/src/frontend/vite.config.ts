import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: resolve(__dirname, '../main/resources/static'),
    emptyOutDir: true,
  },
  // Vitest 配置 - 仅在运行测试时使用
  // @ts-ignore - test 配置由 vitest 读取
  test: {
    globals: true,
    environment: 'jsdom',
  },
})
