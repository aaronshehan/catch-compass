import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Anything starting /api is forwarded to Spring Boot.
      //
      // This is what avoids CORS entirely: the browser only ever talks to
      // localhost:5173, so every request looks same-origin. Calling
      // localhost:8080 directly from the page would be a cross-origin request
      // and would need CORS configuration on the server, plus separate
      // handling for cookies once authentication arrives.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
