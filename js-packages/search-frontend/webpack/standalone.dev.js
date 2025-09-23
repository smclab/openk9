// starts a dev server with automatic reloading
const open = require("open");
const ReactRefreshWebpackPlugin = require("@pmmmwh/react-refresh-webpack-plugin");
const dotenv = require("dotenv");
const webpack = require("webpack");
const path = require("path");

module.exports = (env = {}) => {
  const envPath = path.resolve(__dirname, "../.env");
  const localEnv = dotenv.config({ path: envPath }).parsed || {};
  const finalEnv = {
    ...localEnv,
    ...env,
  };

  const envKeys = Object.keys(finalEnv).reduce((acc, key) => {
    acc[`process.env.${key}`] = JSON.stringify(finalEnv[key]);
    return acc;
  }, {});

  envKeys["process.env.NODE_ENV"] = JSON.stringify("development");

  return {
    mode: "development",
    entry: {
      index: "./src/index.tsx",
    },
    devtool: "inline-source-map",
    module: {
      rules: [
        {
          test: /\.tsx?$/,
          exclude: /node_modules/,
          use: {
            loader: "babel-loader",
            options: {
              plugins: [
                "macros",
                require("react-refresh/babel"),
                [
                  "@babel/plugin-transform-runtime",
                  {
                    regenerator: true,
                  },
                ],
              ],
              presets: [
                "@babel/preset-env",
                "@babel/react",
                "@babel/typescript",
              ],
            },
          },
        },
        {
          test: /\.css$/i,
          use: ["style-loader", "css-loader"],
        },
        {
          test: /\.(png|svg|jpg|jpeg|gif)$/i,
          type: "asset",
        },
      ],
    },
    plugins: [
      new webpack.DefinePlugin(envKeys),
      new ReactRefreshWebpackPlugin(),
    ],
    resolve: {
      extensions: [".tsx", ".ts", ".js"],
    },
    devServer: {
      hot: true,
      historyApiFallback: true,
      static: {
        directory: "./public",
        watch: true,
      },
      proxy: {
        "/api": {
          target: "https://k9-frontend.openk9.io",
          changeOrigin: true,
          secure: false,
        },
      },
      port: 8080,
      onListening: function (devServer) {
        if (!devServer) {
          throw new Error("webpack-dev-server is not defined");
        }
        const port = devServer.server.address().port;
        open(`http://localhost:${port}`);
      },
    },
  };
};
