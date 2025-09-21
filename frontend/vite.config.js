import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    allowedHosts: [
        'algofinserve.com',
        'www.algofinserve.com',
        ],
    port: 3000,
    proxy: {
      '/api': 'http://localhost:8082'
    }
  }
})
