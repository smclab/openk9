import { defineConfig, ProxyOptions } from "vite";
import react from "@vitejs/plugin-react";
import viteCompression from "vite-plugin-compression";

// Strip the WWW-Authenticate header from 401 responses so the browser
// doesn't open its native Basic auth prompt and lets the React app
// show its own /login page / error UI. Authentication itself is done by the
// app (login form -> Authorization: Basic base64(user:pass) on every request,
// see components/client/authStore.tsx), so the proxy only needs to forward it.
const tenantManager = (): ProxyOptions => ({
  target: "https://tenant-manager-frontend.openk9.io",
  changeOrigin: true,
  configure: (proxy) => {
    proxy.on("proxyRes", (proxyRes) => {
      if (proxyRes.statusCode === 401) {
        delete proxyRes.headers["www-authenticate"];
      }
    });
  },
});

export default defineConfig({
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
    // nginx.conf serves the bundle from /admin/static/
    assetsDir: "static",
    rollupOptions: {
      output: {
        manualChunks: (id) => {
          if (!id.includes("node_modules")) return;
          const pkg = (name: string) => id.includes(`node_modules/${name}/`);

          // core React runtime
          if (pkg("react") || pkg("react-dom") || pkg("react-is") || pkg("scheduler")) return "react-core";

          if (pkg("@apollo") || pkg("graphql")) return "apollo";
        },
      },
    },
    chunkSizeWarningLimit: 2000,
  },
  server: {
    port: 3000,
    open: "/admin/",
    proxy: {
      "/api/tenant-manager": tenantManager(),
      "/api/datasource": tenantManager(),
      "/k8s": {
        target: "https://kubernetes-monitoring.openk9.io",
        changeOrigin: true,
      },
    },
  },
});
