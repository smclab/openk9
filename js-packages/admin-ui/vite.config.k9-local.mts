// Dev server pointed at the local ./k9.sh stack instead of the remote demo.
//
//   yarn vite --config vite.config.k9-local.mts   ->  http://localhost:3000/admin/
//
// It has to be selected explicitly: vite only auto-loads the six
// `vite.config.{js,mjs,ts,cjs,mts,cts}` names, so plain `yarn start` keeps
// using `vite.config.ts` and its proxy to the remote demo.
//
// Three things that are not obvious:
//   - the proxy targets 127.0.0.1 while keeping the `demo.openk9.localhost`
//     Host header, because the datasource resolves the tenant from that
//     header and macOS does not resolve *.localhost over DNS;
//   - `secure: false` because caddy serves an internal certificate;
//   - the .mts extension, so the file is loaded as ESM: importing
//     ./vite.config from a .ts file makes esbuild fail (vite and
//     vite-tsconfig-paths are ESM-only), hence the bare minimum is declared
//     here instead of reusing the build config.
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";

const localApi = {
  target: "https://127.0.0.1",
  changeOrigin: false,
  secure: false,
  headers: { Host: "demo.openk9.localhost" },
};

export default defineConfig({
  base: "/admin/",
  plugins: [react(), tsconfigPaths()],
  server: {
    port: 3000,
    open: false,
    proxy: {
      "/api/datasource": localApi,
      "/api/tenant-manager": localApi,
      "/api/searcher": localApi,
      "/api/k8s-client": localApi,
    },
  },
});
