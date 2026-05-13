const { createProxyMiddleware } = require("http-proxy-middleware");

const TENANT_HOST = "https://tenant-manager-frontend.openk9.io";

// Strip the WWW-Authenticate header from 401 responses so the browser
// doesn't open its native Basic auth prompt and lets the React app
// show its own /login page / error UI.
const stripBasicAuthChallenge = (proxyRes) => {
  if (proxyRes.statusCode === 401) {
    delete proxyRes.headers["www-authenticate"];
  }
};

const tenantProxyOptions = {
  target: TENANT_HOST,
  changeOrigin: true,
  on: {
    proxyRes: stripBasicAuthChallenge,
  },
};

module.exports = function (app) {
  app.use("/api/tenant-manager", createProxyMiddleware(tenantProxyOptions));
  app.use("/api/datasource", createProxyMiddleware(tenantProxyOptions));
  app.use(
    "/k8s",
    createProxyMiddleware({
      target: "https://kubernetes-monitoring.openk9.io",
      changeOrigin: true,
    })
  );
};
