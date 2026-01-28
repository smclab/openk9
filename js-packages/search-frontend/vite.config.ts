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
// Starts a dev server with automatic reloading
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

const ReactCompilerConfig = {};

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, path.resolve(__dirname, ".."), "");

  return {
    plugins: [
      react({
        babel: {
          plugins: [
            [
              "babel-plugin-react-compiler",
              ReactCompilerConfig,
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
      __DEV__: true,
      "import.meta.env.NODE_ENV": JSON.stringify("development"),
      global: "globalThis",
    },

    resolve: {
      extensions: [".tsx", ".ts", ".jsx", ".js"],
      alias: {
        "@": path.resolve(__dirname, "../src"),
      },
    },

    root: path.resolve(__dirname, ".."),
    publicDir: "public",

    server: {
      port: 8080,
      open: true,
      proxy: {
        "/api": {
          target: "https://k9-frontend.openk9.io",
          changeOrigin: true,
          secure: false,
        },
      },
    },

    build: {
      sourcemap: true,
    },

    optimizeDeps: {
      include: [
        "react",
        "react-dom",
        "react/jsx-runtime",
        "styled-components",
        "lodash",
        "luxon",
        "moment",
      ],
      esbuildOptions: {
        target: "esnext",
      },
    },
  };
});

