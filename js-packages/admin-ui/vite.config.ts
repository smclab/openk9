import { defineConfig, PluginOption } from "vite";
import react from "@vitejs/plugin-react";
import viteCompression from "vite-plugin-compression";
import tsconfigPaths from "vite-tsconfig-paths";

// Where the dev server proxies /api/* to: the remote demo by default, the
// local ./k9.sh stack with `yarn start:k9-local` (i.e. `vite --mode k9-local`).
//
// Two things about the local stack that are not obvious:
//   - the proxy targets 127.0.0.1 while keeping the `demo.openk9.localhost`
//     Host header, because the datasource resolves the tenant from that
//     header and macOS does not resolve *.localhost over DNS;
//   - `secure: false` because caddy serves an internal certificate.
const api = (mode: string) =>
  mode === "k9-local"
    ? {
        target: "https://127.0.0.1",
        changeOrigin: false,
        secure: false,
        headers: { Host: "demo.openk9.localhost" },
      }
    : {
        target: "https://k9-frontend.openk9.io",
        changeOrigin: true,
      };

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
          const pkg = (name: string) => id.includes(`node_modules/${name}/`);

          if (pkg("monaco-editor")) return "monaco-editor";

          // core React runtime
          if (pkg("react") || pkg("react-dom") || pkg("react-is") || pkg("scheduler"))
            return "react-core";

          if (pkg("@apollo") || pkg("graphql")) return "apollo";

          if (pkg("recharts") || id.includes("node_modules/d3-")) return "recharts";
        },
      },
    },
    chunkSizeWarningLimit: 2000,
  },
  server: {
    port: 3000,
    open: "/admin/",
    proxy: {
      "/api/datasource": api(mode),
      "/api/tenant-manager": api(mode),
      "/api/searcher": api(mode),
      "/api/k8s-client": api(mode),
    },
  },
}));