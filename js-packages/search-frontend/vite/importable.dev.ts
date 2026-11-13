import { defineConfig, esmExternalRequirePlugin } from "vite";
import react from "@vitejs/plugin-react";
import babel from "@rolldown/plugin-babel";
import path from "path";
import { noCustomOutput } from "./plugins/noCustomOutput";

export default defineConfig(() => {
  return {
    plugins: [
      react(),
      babel({
        plugins: [
          [
            "babel-plugin-styled-components",
            { displayName: true, fileName: true, ssr: false },
          ],
        ],
      }),
      // Vite 8 keeps require() of externals in the ES build: turn them into imports
      esmExternalRequirePlugin({
        external: [
          "react",
          "react-dom",
          "react/jsx-runtime",
          "styled-components",
          "i18next",
          "lodash",
        ],
      }),
      noCustomOutput(),
    ],
    resolve: { extensions: [".tsx", ".ts", ".js"] },
    // bundled CommonJS code reads global, which Vite 7 rewrote and Vite 8 leaves to the browser
    define: { global: "globalThis" },
    build: {
      sourcemap: true,
      minify: false,
      outDir: "dist",
      emptyOutDir: false,
      cssCodeSplit: true,
      lib: {
        entry: path.resolve(__dirname, "../src/index.ts"),
        name: "OpenK9SearchFrontend",
        formats: ["es", "cjs"],
        fileName: (format) => `index.${format}.js`,
      },
      rolldownOptions: {
        output: {
          entryFileNames: "index.[format].js",
          globals: {
            react: "React",
            "react-dom": "ReactDOM",
            "styled-components": "styled",
          },
        },
      },
    },
  };
});
