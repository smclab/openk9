const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin");
// const BundleAnalyzerPlugin = require("webpack-bundle-analyzer").BundleAnalyzerPlugin;

module.exports = {
  webpack: {
    plugins: {
      add: [
        [new MonacoWebpackPlugin(), "append"],
        // [new BundleAnalyzerPlugin(), "append"],
      ],
    },
  },
};
