/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import dts from "vite-plugin-dts";
import { libInjectCss } from "vite-plugin-lib-inject-css";
import { extname, relative, resolve } from "path";
import { fileURLToPath } from "node:url";
import { glob } from "glob";
import commonjs from "@rollup/plugin-commonjs";

export default defineConfig({
  plugins: [
    react(),
    libInjectCss(),
    dts({ include: ["lib"], outDir: "dist" }),
    commonjs({
      include: /node_modules/,
      transformMixedEsModules: true,
      requireReturnsDefault: "auto",
    }),
  ],
  server: {
    proxy: {
      "/api": {
        target: "https://k9-ai.openk9.io",
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    copyPublicDir: false,
    emptyOutDir: false,
    minify: false,
    rollupOptions: {
      external: [
        "react",
        "react/jsx-runtime",
        "@mui/material",
        "@mui/system",
        "@emotion/react",
        "@emotion/styled",
        "style-to-object",
        "extend",
        "prop-types",
        "react-is",
      ],
      output: {
        assetFileNames: "assets/[name][extname]",
        entryFileNames: "[name].js",
      },
      input: Object.fromEntries(
        glob
          .sync("lib/**/*.{ts,tsx}", {
            ignore: ["lib/**/*.d.ts"],
          })
          .map((file) => [
            relative("lib", file.slice(0, file.length - extname(file).length)),
            fileURLToPath(new URL(file, import.meta.url)),
          ]),
      ),
    },
    lib: {
      entry: resolve(__dirname, "lib/main.ts"),
      formats: ["es"],
    },
  },
 
  optimizeDeps: {
    include: [
      "prop-types",
      "react-is",
      "@mui/material",
      "@mui/system",
      "@mui/utils",
    ],
  },
});

