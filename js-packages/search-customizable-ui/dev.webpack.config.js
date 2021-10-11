const path = require("path")
const config = require("./webpack.config")

module.exports = {
  ...config,
  mode: "development",
  devtool: "inline-source-map",
  devServer: {
    static: {
      directory: path.join(__dirname, 'public'),
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
