const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(
    "/api/*",
    createProxyMiddleware({
      target: "http://dev-projectq.smc.it/",
      changeOrigin: true,
    }),
  );
};
