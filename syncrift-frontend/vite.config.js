import path from "path"
import tailwindcss from "@tailwindcss/vite"
import react from "@vitejs/plugin-react"
import { defineConfig } from "vite"
import nodePolyfills from 'rollup-plugin-node-polyfills';

// https://vite.dev/config/
export default defineConfig({
  optimizeDeps: {
    include: ['buffer', 'process'] // needed for many polyfills
  },
  build: {
    rollupOptions: {
      plugins: [nodePolyfills()]
    }
  },
  define: {
    global: 'window'
  },
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
})