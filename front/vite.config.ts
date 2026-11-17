import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    // En dev local : services lancés via docker (8081 catalogue, 8082 emprunts)
    proxy: {
      '/api/livres': 'http://localhost:8081',
      '/api/emprunts': 'http://localhost:8082',
    },
  },
})
