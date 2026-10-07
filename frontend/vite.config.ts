import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Locally the built UI is served by Spring Boot from the same origin (no CORS).
// On Netlify, VITE_OUT_DIR=dist and VITE_API_BASE points at the Render API (see netlify.toml).
export default defineConfig({
  plugins: [react()],
  build: {
    outDir: process.env.VITE_OUT_DIR ?? '../src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    port: 5180,
    proxy: { '/api': 'http://127.0.0.1:8090' },
  },
})
