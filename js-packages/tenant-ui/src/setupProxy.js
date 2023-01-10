const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(
    "/api/tenant-manager/graphql",
    createProxyMiddleware({
      target: "https://tenant-manager.openk9.io",
      changeOrigin: true,
    })
  );
  app.use(
    "/api/tenant-manager/oauth2",
    createProxyMiddleware({
      target: "https://tenant-manager.openk9.io",
      changeOrigin: true,
    })
  );
  app.use(
    "/api/tenant-manager/tenant-manager",
    createProxyMiddleware({
      target: "https://tenant-manager.openk9.io",
      changeOrigin: true,
    })
  )
  app.use(
    "/k8s",
    createProxyMiddleware({
      target: "https://kubernetes-monitoring.openk9.io",
      changeOrigin: true,
    })
  );
};
