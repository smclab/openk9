import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

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
      cssCodeSplit: false,
    },
  };
});
