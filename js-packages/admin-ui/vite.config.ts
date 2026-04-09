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
        manualChunks: (id) => {
          if (!id.includes("node_modules")) return;

          // exact-package match (trailing slash avoids react ≠ react-is)
          const pkg = (name: string) => id.includes(`node_modules/${name}/`);

          if (pkg("monaco-editor")) return "monaco-editor";

          // core React runtime — must stay in one chunk
          if (pkg("react") || pkg("react-dom") || pkg("react-is") || pkg("scheduler"))
            return "react-core";

          if (pkg("@apollo") || pkg("graphql")) return "apollo";

          if (pkg("recharts") || id.includes("node_modules/d3-")) return "recharts";

          // no catch-all: let Vite handle the rest
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