const { createProxyMiddleware } = require("http-proxy-middleware");

const TENANT_HOST = "http://demo2.openk9.localhost";

module.exports = function (app) {
  app.use(
    "/api/tenant-manager",
    createProxyMiddleware({
      target: TENANT_HOST,
      changeOrigin: true,
    })
  );
  app.use(
    "/api/datasource",
    createProxyMiddleware({
      target: TENANT_HOST,
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
