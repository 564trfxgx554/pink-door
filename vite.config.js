import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Pink Door — Vue 3 + Vite
// Dev server runs on http://localhost:5173 by default.
// The Java backend is expected at http://localhost:8080 (see .env.example).
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // Uncomment the proxy below if you prefer the frontend to call
    // /api/... instead of http://localhost:8080 directly.
    // proxy: {
    //   '/api': 'http://localhost:8080'
    // }
  }
})
