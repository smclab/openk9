const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(
    createProxyMiddleware("/api", {
      target: "http://dev-projectq.smc.it/",
      // target: "https://demo.openk9.io/",
      changeOrigin: true,
      secure: false,
    }),
  );
};
