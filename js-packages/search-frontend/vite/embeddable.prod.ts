// Builds a script with react inside for use with <script src=""/>
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import babel from "@rolldown/plugin-babel";
import path from "path";
import license from "rollup-plugin-license";
import { noCustomOutput } from "./plugins/noCustomOutput";

export default defineConfig({
  plugins: [
    react(),
    // without it styled-components' css prop and keyframes break in the UMD
    babel({
      plugins: [
        [
          "babel-plugin-styled-components",
          {
            displayName: true,
            fileName: true,
          },
        ],
      ],
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
    noCustomOutput(),
  ],
  resolve: {
    extensions: [".tsx", ".ts", ".js"],
  },
  define: {
    "process.env": {},
    // Vite 8 leaves process.env.NODE_ENV in lib builds: keep it undefined, as Vite 7 did
    "process.env.NODE_ENV": "undefined",
    // moment's locale wrappers test typeof require, which Vite 7 rewrote and Vite 8 leaves to the browser
    "typeof require": JSON.stringify("function"),
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
    rolldownOptions: {
      external: [],
      output: {
        exports: "named",
      },
    },
  },

  optimizeDeps: { disabled: true },
});
