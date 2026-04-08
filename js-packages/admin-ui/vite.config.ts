import { defineConfig, PluginOption } from "vite";
import react from "@vitejs/plugin-react";
import viteCompression from "vite-plugin-compression";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig(({ mode }) => ({
  base: "/admin/",
  plugins: [
    {
      name: "redirect-root-to-admin",
      configureServer(server) {
        server.middlewares.use((req, res, next) => {
          if (req.url === "/" || req.url === "") {
            res.writeHead(302, { Location: "/admin/" });
            res.end();
            return;
          }
          next();
        });
      },
    },
    react(),
    tsconfigPaths(),
    viteCompression({
      algorithm: "gzip",
      ext: ".gz",
      threshold: 10240,
    }),
    viteCompression({
      algorithm: "brotliCompress",
      ext: ".br",
      threshold: 10240,
    }),
  ],
  build: {
    outDir: "build",
    rollupOptions: {
      output: {
        manualChunks: {
          "monaco-editor": ["monaco-editor"],
        },
      },
    },
    chunkSizeWarningLimit: 2000,
  },
  server: {
    port: 3000,
    open: "/admin/",
    proxy: {
      "/api/datasource": {
        target: "https://k9-frontend.openk9.io",
        changeOrigin: true,
      },
      "/api/k8s-client": {
        target: "https://k9-frontend.openk9.io",
        changeOrigin: true,
      },
    },
  },
}));