// Builds a script with react inside for use with <script src=""/>
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import license from "rollup-plugin-license";

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
    license({
      thirdParty: {
        allow: (name) => true,
        output: [
          {
            file: path.join(__dirname, "../dist/embeddable.js.LICENSE.txt"),
          },
        ],
      },
    }),
  ],

  define: {
    "process.env": {},
    global: "globalThis",
  },
  build: {
    minify: false,
    sourcemap: true,
    cssCodeSplit: false,
    emptyOutDir: false,
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
