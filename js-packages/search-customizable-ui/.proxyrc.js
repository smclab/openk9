const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(
    createProxyMiddleware("/api", {
      // target: "https://demo.openk9.io/",
      target: "https://dev.openk9.io/",
      changeOrigin: true,
      secure: false,
    }),
  );
};
