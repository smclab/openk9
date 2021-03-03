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
 *
 * Copyright (c) 2015-present, Facebook, Inc.
 */

"use strict";

// Do this as the first thing so that any code reading it knows the right env.
process.env.BABEL_ENV = "production";
process.env.NODE_ENV = "production";

// Makes the script crash on unhandled rejections instead of silently
// ignoring them. In the future, promise rejections that are not handled will
// terminate the Node.js process with a non-zero exit code.
process.on("unhandledRejection", (err) => {
  throw err;
});

const path = require("path");
const chalk = require("react-dev-utils/chalk");
const fs = require("fs-extra");
const printBuildError = require("react-dev-utils/printBuildError");
const rollup = require("rollup");

const argv = process.argv.slice(2);
const buildGraph = argv.indexOf("--graph") !== -1;

const appDirectory = fs.realpathSync(process.cwd());
const resolveApp = (relativePath) => path.resolve(appDirectory, relativePath);
const publicPath = resolveApp("public");
const buildPath = resolveApp("build");
const srcPath = resolveApp("src");

if (!fs.existsSync(publicPath)) {
  fs.mkdirSync(publicPath);
}

// Remove all content but keep the directory so that
// if you're in it, you don't end up in Trash
fs.emptyDirSync(buildPath);
// Merge with the public folder
copyPublicFolder();
// Start the webpack build
build()
  .then(
    () => {
      console.log(chalk.green("Compiled successfully.\n"));
    },
    (err) => {
      const tscCompileOnError = process.env.TSC_COMPILE_ON_ERROR === "true";
      if (tscCompileOnError) {
        console.log(
          chalk.yellow(
            "Compiled with the following type errors (you may want to check these before deploying your app):\n",
          ),
        );
        printBuildError(err);
      } else {
        console.log(chalk.red("Failed to compile.\n"));
        printBuildError(err);
        process.exit(1);
      }
    },
  )
  .catch((err) => {
    if (err && err.message) {
      console.log(err.message);
    }
    process.exit(1);
  });

// Create the production build and print the deployment instructions.
async function build() {
  console.log("Creating an optimized production build...");

  const rollupConfig = require("../config/rollup.config")(
    srcPath,
    buildPath,
    buildGraph,
  );

  await Promise.all(
    rollupConfig.map(async (config) => {
      const { output: outputOptions, ...inputOptions } = config;
      const bundle = await rollup.rollup(inputOptions);
      const { output } = await bundle.generate(outputOptions);
      await bundle.write(outputOptions);
      return output;
    }),
  );
}

function copyPublicFolder() {
  fs.copySync(publicPath, buildPath, {
    dereference: true,
  });
}
