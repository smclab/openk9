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

const { babel } = require("@rollup/plugin-babel");
const { nodeResolve: resolve } = require("@rollup/plugin-node-resolve");
const json = require("@rollup/plugin-json");
const replace = require("@rollup/plugin-replace");
const commonjs = require("@rollup/plugin-commonjs");
const externalGlobals = require("rollup-plugin-external-globals");
const { terser } = require("rollup-plugin-terser");

const extensions = [".js", ".jsx", ".ts", ".tsx", ".json"];

const getBabelOptions = ({ useESModules }, targets) => ({
  babelrc: false,
  extensions,
  exclude: "**/node_modules/**",
  babelHelpers: "runtime",
  presets: [
    [require("@babel/preset-env"), { loose: true, modules: false, targets }],
    require("@babel/preset-react"),
    require("@babel/preset-typescript"),
  ],
  plugins: [
    [
      require("@babel/plugin-transform-runtime"),
      { regenerator: false, useESModules },
    ],
  ],
});

const externalGlobalsDefault = {
  react: "React",
  "@openk9/http-api": "ok9API",
  "@openk9/search-ui-components": "ok9Components",
  "@clayui/icon": "clayIcon",
  "react-jss": "reactJSS",
};

module.exports = (srcPath, buildPath) => [
  {
    input: `${srcPath}/index.tsx`,
    output: { file: `${buildPath}/index.js`, format: "esm" },
    external: Object.keys(externalGlobalsDefault),
    plugins: [
      json(),
      babel(
        getBabelOptions(
          { useESModules: true },
          ">1%, not dead, not ie 11, not op_mini all",
        ),
      ),
      resolve({ extensions }),
      commonjs(),
      externalGlobals(externalGlobalsDefault),
      replace({
        "process.env.NODE_ENV": JSON.stringify("production"),
        preventAssignment: true,
      }),
      terser(),
    ],
  },
  {
    input: `${srcPath}/index.tsx`,
    output: { file: `${buildPath}/index.cjs.js`, format: "cjs" },
    external: Object.keys(externalGlobalsDefault),
    plugins: [
      json(),
      babel(getBabelOptions({ useESModules: false })),
      resolve({ extensions }),
      commonjs(),
      externalGlobals(externalGlobalsDefault),
      replace({
        "process.env.NODE_ENV": JSON.stringify("production"),
        preventAssignment: true,
      }),
      terser(),
    ],
  },
];
