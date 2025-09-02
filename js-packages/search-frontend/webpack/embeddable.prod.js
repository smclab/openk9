// builds a script with react inside for use with <script src=""/>

const path = require("path");
const webpack = require("webpack");
const dotenv = require("dotenv");
const fs = require("fs");

module.exports = () => {
  const envFilePath = path.resolve(__dirname, "../.env");
  const fileEnv = fs.existsSync(envFilePath)
    ? dotenv.config({ path: envFilePath }).parsed
    : {};

  const envKeys = Object.keys(fileEnv || {}).reduce((prev, next) => {
    prev[`process.env.${next}`] = JSON.stringify(fileEnv[next]);
    return prev;
  }, {});

  return {
    mode: "production",
    entry: {
      embeddable: "./src/embeddable/entry.tsx",
    },
    devtool: "source-map",
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
          type: "asset/inline",
        },
      ],
    },
    resolve: {
      extensions: [".tsx", ".ts", ".js"],
    },
    output: {
      path: path.resolve(__dirname, "../dist"),
      filename: "[name].js",
      libraryTarget: "umd",
    },
    plugins: [new webpack.DefinePlugin(envKeys)],
  };
};
