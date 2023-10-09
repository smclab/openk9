// starts a dev server with automatic reloading

const ReactRefreshWebpackPlugin = require("@pmmmwh/react-refresh-webpack-plugin");

module.exports = {
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
            presets: ["@babel/preset-env", "@babel/react", "@babel/typescript"],
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
  plugins: [new ReactRefreshWebpackPlugin()],
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
        target: "https://test.openk9.io",
        changeOrigin: true,
        secure: false,
      },
    },
  },
};
