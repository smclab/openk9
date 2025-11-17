// Starts a dev server with automatic reloading
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

const ReactCompilerConfig = {};

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, path.resolve(__dirname, ".."), "");

  return {
    plugins: [
      react({
        babel: {
          plugins: [
            [
              "babel-plugin-react-compiler",
              ReactCompilerConfig,
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
      __DEV__: true,
      "import.meta.env.NODE_ENV": JSON.stringify("development"),
      global: "globalThis",
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
      ],
      esbuildOptions: {
        target: "esnext",
      },
    },
  };
});
