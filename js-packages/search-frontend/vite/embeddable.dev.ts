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
  resolve: {
    extensions: [".tsx", ".ts", ".js"],
  },
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
      external: [
        "react",
        "react-dom",
        "react/jsx-runtime",
        "styled-components",
        "react-query",
        "i18next",
        "lodash",
      ],
      output: {
        exports: "named",
      },
    },
  },
});
