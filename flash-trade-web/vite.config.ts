import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig(({ command }) => {
  return {
    plugins: [vue()],
    base: '/quicktrade/admin/',

    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },

    server: {
      port: 5174,
      proxy: {
        '/QuickTrade/web': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          rewrite: path => path
        }
      }
    },

    build: {
      outDir: 'dist',
      emptyOutDir: true
    }
  }
})
