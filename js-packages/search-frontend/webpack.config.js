const path = require("path");
const ReactRefreshWebpackPlugin = require("@pmmmwh/react-refresh-webpack-plugin");

const isProd = process.env.PRODUCTION === "true";

module.exports = {
  mode: isProd ? "production" : "development",
  entry: "./src/index.tsx",
  devtool: isProd ? "source-map" : "inline-source-map",
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
              isProd ? null : require("react-refresh/babel"),
              [
                "@babel/plugin-transform-runtime",
                {
                  regenerator: true,
                },
              ],
            ].filter(Boolean),
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
        type: "asset/inline",
      },
    ],
  },
  plugins: isProd ? [] : [new ReactRefreshWebpackPlugin()],
  resolve: {
    extensions: [".tsx", ".ts", ".js"],
  },
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "index.js",
    libraryTarget: "umd",
  },
  devServer: {
    hot: true,
    static: {
      directory: path.join(__dirname, "public"),
      watch: true,
    },
    proxy: {
      "/api": {
        target: "https://dev.openk9.io",
        changeOrigin: true,
        secure: false,
      },
    },
  },
};
