const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(
    "/api/datasource/",
    createProxyMiddleware({
      target: "https://k9-test-301-7.openk9.io",
      changeOrigin: true,
    }),
  );
  app.use(
    "/api/k8s-client",
    createProxyMiddleware({
      target: "https://k9-test-301-7.openk9.io",
      changeOrigin: true,
    }),
  );
};
