import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import license from "rollup-plugin-license";

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
      license({
        thirdParty: {
          allow: (name) => true,
          output: [
            {
              file: path.join(__dirname, "../dist/index.js.LICENSE.txt"),
            },
          ],
        },
      }),
    ],
    define: {
      global: "globalThis",
      "process.env.NODE_ENV": JSON.stringify(mode),
    },
    resolve: {
      extensions: [".tsx", ".ts", ".js"],
    },
    root: path.resolve(__dirname, ".."),
    publicDir: "public",
    build: {
      sourcemap: true,
      outDir: "dist",
      emptyOutDir: false,
      cssCodeSplit: false,
      rollupOptions: {
        input: {
          index: path.resolve(__dirname, "../index.html"),
        },
        output: {
          entryFileNames: "[name].js",
          assetFileNames: "[name].[ext]",
        },
      },
    },
  };
});
