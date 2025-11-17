// Builds a script with react inside for use with <script src=""/>
import { defineConfig } from "vite";
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

  define: {
    "process.env": {},
    global: "globalThis",
  },

  resolve: {
    extensions: [".tsx", ".ts", ".js"],
  },

  build: {
    target: "es2018",
    minify: true,
    sourcemap: true,
    cssCodeSplit: false,
    outDir: "dist",
    lib: {
      entry: path.resolve(__dirname, "../src/embeddable/entry.tsx"),
      name: "OpenK9Embeddable",
      formats: ["umd"],
      fileName: () => "embeddable.js",
    },
    rollupOptions: {
      external: [],

      output: {
        inlineDynamicImports: true,

        exports: "named",
      },
    },
  },

  optimizeDeps: { disabled: true },
});
