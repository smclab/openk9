import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import { noCustomOutput } from "./plugins/noCustomOutput";

export default defineConfig(() => {
  return {
    plugins: [
      react({
        babel: {
          plugins: [
            [
              "babel-plugin-styled-components",
              { displayName: true, fileName: true, ssr: false },
            ],
          ],
        },
      }),
      noCustomOutput(),
    ],
    resolve: { extensions: [".tsx", ".ts", ".js"] },
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
