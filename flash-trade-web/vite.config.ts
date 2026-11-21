import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

const apiProxy = () => ({
  target: 'http://localhost:8080',
  changeOrigin: true
})

export default defineConfig(() => {
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
        '/api': apiProxy(),
        '/admin': apiProxy(),
        '/users': apiProxy(),
        '/positions': apiProxy()
      }
    },

    build: {
      outDir: 'dist',
      emptyOutDir: true
    }
  }
})
