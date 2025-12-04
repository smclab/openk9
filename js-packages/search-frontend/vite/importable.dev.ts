import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import { noCustomOutput } from "./plugins/noCustomOutput";

export default defineConfig(() => {
  return {
    plugins: [react(), noCustomOutput()],
    resolve: {
      extensions: [".tsx", ".ts", ".js"],
    },
    build: {
      sourcemap: true,
      minify: false,
      outDir: "dist",
      emptyOutDir: false,
      cssCodeSplit: true,
      lib: {
        entry: {
          importable: path.resolve(__dirname, "../src/index.ts"),
        },
        name: "OpenK9SearchFrontend",
        formats: ["es", "cjs"],
        fileName: (format) => `importable.${format}.js`,
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
          path: path.resolve(__dirname, "../dist"),
          entryFileNames: "[name].js",
          libraryTarget: "umd",
        },
      },
    },
  };
});
