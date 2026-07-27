// Dev server puntato allo stack ./k9.sh locale invece che al demo remoto.
//
//   yarn vite --config vite.config.k9-local.mts   ->  http://localhost:3000/admin/
//
// Va scelta esplicitamente: vite auto-carica solo i sei nomi
// `vite.config.{js,mjs,ts,cjs,mts,cts}`, quindi `yarn start` continua a usare
// `vite.config.ts` e il suo proxy verso il demo remoto.
//
// Tre dettagli non ovvi:
//   - il proxy punta a 127.0.0.1 tenendo l'Host `demo.openk9.localhost`,
//     perche' e' da quell'header che il datasource risolve il tenant e su
//     macOS *.localhost non si risolve via DNS;
//   - `secure: false` perche' caddy usa un certificato interno;
//   - estensione .mts, cioe' caricata come ESM: importare ./vite.config da un
//     file .ts fa fallire esbuild (vite e vite-tsconfig-paths sono ESM-only),
//     percio' qui il minimo indispensabile e' ridichiarato invece di riusare
//     la config di build.
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
