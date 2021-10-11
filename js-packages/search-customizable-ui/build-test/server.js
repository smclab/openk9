const express = require("express");
const { createProxyMiddleware } = require("http-proxy-middleware");

var app = express();
app.use(
  createProxyMiddleware("/api", {
    target: "https://dev.openk9.io/",
    changeOrigin: true,
    secure: false,
  }),
);
app.use(express.static("public"));
app.use(express.static("dist"));
app.listen(3001, () => console.log("listening on port 3001"));
