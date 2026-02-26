import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
  plugins: [
    react({
      babel: {
        plugins: [
          "babel-plugin-macros",
          ["@babel/plugin-transform-runtime", { regenerator: true }],
        ],
      },
    }),
  ],
  build: {
    sourcemap: true,
    minify: false,
    outDir: "dist",
    cssCodeSplit: false,
    lib: {
      entry: path.resolve(__dirname, "../src/embeddable/entry.tsx"),
      name: "OpenK9Embeddable",
      formats: ["umd"],
      fileName: () => `embeddable.js`,
    },
    rollupOptions: {
      output: {
        globals: {
          react: "React",
          "react-dom": "ReactDOM",
          "react/jsx-runtime": "jsxRuntime",
          "styled-components": "styled",
          "react-query": "ReactQuery",
          i18next: "i18next",
          lodash: "_",
        },
        exports: "named",
      },
    },
  },
});
