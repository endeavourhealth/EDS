import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
      'font-awesome-css': path.resolve(__dirname, 'src/fontawesome.css'),
    },
    extensions: ['.ts', '.tsx', '.js', '.jsx', '.json', '.vue'],
  },
  base: './',
  build: {
    outDir: '../src/main/webapp',
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router', 'pinia'],
          keycloak: ['keycloak-js'],
        },
      },
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/public': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
