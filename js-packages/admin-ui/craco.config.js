const path = require("path");
// const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin");

module.exports = {
  webpack: {
    alias: {
      "@components": path.resolve(__dirname, "src/components"),
      "@pages": path.resolve(__dirname, "src/pages"),
    },
    // plugins: {
    //   add: [[new MonacoWebpackPlugin(), "append"]],
    // },
  },
};
