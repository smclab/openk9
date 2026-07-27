// Config usa-e-getta per sviluppare contro lo stack k9.sh locale.
// Non committare. Estensione .mts perche' caricata come ESM: importare
// ./vite.config da un file .ts fa fallire esbuild (vite e
// vite-tsconfig-paths sono ESM-only), quindi qui il minimo indispensabile
// e' ridichiarato invece di riusare la config di build.
//
//   yarn vite --config vite.config.local.mts
//
// Il proxy punta a 127.0.0.1 tenendo l'Host `demo.openk9.localhost`: e' da
// quell'header che il datasource risolve il tenant, e su macOS *.localhost
// non si risolve via DNS.
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
