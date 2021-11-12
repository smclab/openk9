const { createProxyMiddleware } = require("http-proxy-middleware");
const app = require("express")();

app.use(
  "/api/*",
  createProxyMiddleware({
    target: "https://dev.openk9.io/",
    changeOrigin: true,
    secure: false,
  }),
);

app.use(
  "/*",
  createProxyMiddleware({
    target: "http://localhost:3000/",
    changeOrigin: true,
    secure: false,
  }),
);

app.listen(8080);
