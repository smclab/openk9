const { createProxyMiddleware } = require("http-proxy-middleware");

const TENANT_HOST = "https://tenant-manager-frontend.openk9.io";

// Strip the WWW-Authenticate header from 401 responses so the browser
// doesn't open its native Basic auth prompt and lets the React app
// show its own /login page / error UI. Authentication itself is done by the
// app (login form -> Authorization: Basic base64(user:pass) on every request,
// see components/client/authStore.tsx), so the proxy only needs to forward it.
const stripBasicAuthChallenge = (proxyRes) => {
  if (proxyRes.statusCode === 401) {
    delete proxyRes.headers["www-authenticate"];
  }
};

// http-proxy-middleware v4 forwards req.url, not req.originalUrl. Mounting
// with an Express path (app.use("/api/tenant-manager", ...)) makes Express strip
// that prefix from req.url, so the backend would receive "/graphql" instead of
// "/api/tenant-manager/graphql" -> 404. Mount WITHOUT a path and select requests
// with pathFilter, so the full path is preserved and forwarded upstream.
const tenantProxyOptions = {
  target: TENANT_HOST,
  changeOrigin: true,
  pathFilter: (pathname) => pathname.startsWith("/api/tenant-manager") || pathname.startsWith("/api/datasource"),
  on: {
    proxyRes: stripBasicAuthChallenge,
  },
};

module.exports = function (app) {
  app.use(createProxyMiddleware(tenantProxyOptions));
  app.use(
    createProxyMiddleware({
      target: "https://kubernetes-monitoring.openk9.io",
      changeOrigin: true,
      pathFilter: "/k8s",
    })
  );
};
