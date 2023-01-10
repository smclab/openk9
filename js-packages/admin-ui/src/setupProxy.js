const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(
    "/api/datasource/graphql",
    createProxyMiddleware({
      target: "https://test.openk9.io",
      changeOrigin: true,
    })
  );
  app.use(
    "/api/datasource/v1",
    createProxyMiddleware({
      target: "https://test.openk9.io",
      changeOrigin: true,
    })
  );
  app.use(
    "/api/datasource/oauth2",
    createProxyMiddleware({
      target: "https://test.openk9.io",
      changeOrigin: true,
    })
  );
  app.use(
    "/k8s",
    createProxyMiddleware({
      target: "https://test.openk9.io",
      changeOrigin: true,
    })
  );
};
