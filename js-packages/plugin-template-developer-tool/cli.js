#!/usr/bin/env node

const Webpack = require("webpack");
const WebpackDevServer = require("webpack-dev-server");
const webpackConfig = require("./webpack.config.js");

const [, , command, ...args] = process.argv;

switch (command) {
  case "dev": {
    const [proxyTarget] = args;
    const config = webpackConfig({ proxyTarget });
    const compiler = Webpack(config);
    const devServerOptions = { ...config.devServer, open: true };
    const server = new WebpackDevServer(devServerOptions, compiler);
    server.start();
    break;
  }
  default: {
    console.error("unknown command " + command);
    console.log("supported commands:");
    console.log(
      "dev - start a webpack development server using ./src/template-examples.tsx as entry",
    );
  }
}
