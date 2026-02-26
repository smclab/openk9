// Starts a dev server with automatic reloading
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig(({ mode }) => {
  return {
    plugins: [
      react({
        babel: {
          plugins: [
            [
              "babel-plugin-styled-components",
              {
                displayName: true,
                fileName: true,
              },
            ],
          ],
        },
      }),
    ],

    define: {
      global: "globalThis",
      "process.env.NODE_ENV": JSON.stringify(mode),
    },

    resolve: {
      extensions: [".tsx", ".ts", ".jsx", ".js"],
      alias: {
        "@": path.resolve(__dirname, "../src"),
      },
    },

    root: path.resolve(__dirname, ".."),
    publicDir: "public",

    server: {
      port: 8080,
      open: true,
      proxy: {
        "/api": {
          target: "https://k9-frontend.openk9.io",
          changeOrigin: true,
          secure: false,
        },
      },
    },

    build: {
      sourcemap: true,
    },

    optimizeDeps: {
      include: [
        "react",
        "react-dom",
        "react/jsx-runtime",
        "styled-components",
        "lodash",
        "luxon",
        "moment",
        "react-dates",
      ],
      esbuildOptions: {
        target: "esnext",
        define: {
          global: "globalThis",
        },
      },
    },
  };
});
