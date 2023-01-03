const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(
    "/graphql",
    createProxyMiddleware({
      target: "https://test.openk9.io",
      changeOrigin: true,
    })
  );
  app.use(
    "/v1",
    createProxyMiddleware({
      target: "https://test.openk9.io",
      changeOrigin: true,
    })
  );
  app.use(
    "/oauth2",
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
