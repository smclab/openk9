const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(
    "/api/tenant-manager",
    createProxyMiddleware({
      target: "https://tenant-manager-stable-2.openk9.io",
      changeOrigin: true,
    })
  );
  app.use(
    "/k8s",
    createProxyMiddleware({
      target: "https://kubernetes-monitoring.openk9.io",
      changeOrigin: true,
    })
  );
};
