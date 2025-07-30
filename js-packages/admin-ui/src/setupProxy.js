const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(
    "/api/datasource/",
    createProxyMiddleware({
      target: "https://k9-frontend.openk9.io",
      changeOrigin: true,
    }),
  );
  app.use(
    "/api/k8s-client",
    createProxyMiddleware({
      target: "https://k9-frontend.openk9.io",
      changeOrigin: true,
    }),
  );
};
