const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin");
const path = require("path");
// const BundleAnalyzerPlugin = require("webpack-bundle-analyzer").BundleAnalyzerPlugin;

module.exports = {
  webpack: {
    alias: {
      "@components": path.resolve(__dirname, "src/components"),
      "@pages": path.resolve(__dirname, "src/pages"),
    },
    plugins: {
      add: [
        [new MonacoWebpackPlugin(), "append"],
        // [new BundleAnalyzerPlugin(), "append"],
      ],
    },
  },
};
