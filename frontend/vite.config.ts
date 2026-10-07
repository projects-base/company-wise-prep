import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The built UI is served by Spring Boot from the same origin, so no CORS anywhere.
export default defineConfig({
  plugins: [react()],
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    port: 5180,
    proxy: { '/api': 'http://127.0.0.1:8090' },
  },
})
