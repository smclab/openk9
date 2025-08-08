// builds production version of the standalone app

const path = require("path");

module.exports = (env = {}) => {
  const isKeycloakEnabled = env.keycloak === "true";
  const isChatbotEnabled = env.chatbot === "true";

  return {
    mode: "production",
    entry: {
      index: "./src/index.tsx",
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
    // decomment this to analyze bundle size
    // plugins: [new require("webpack-bundle-analyzer").BundleAnalyzerPlugin()],
    resolve: {
      extensions: [".tsx", ".ts", ".js"],
    },
    output: {
      path: path.resolve(__dirname, "../dist"),
      filename: "[name].js",
      libraryTarget: "umd",
    },
    plugins: [
      new webpack.DefinePlugin({
        "process.env.REACT_APP_KEYCLOAK_ENABLED":
          JSON.stringify(isKeycloakEnabled),
        "process.env.REACT_APP_CHATBOT_ENABLED":
          JSON.stringify(isChatbotEnabled),
      }),
    ],
  };
};
