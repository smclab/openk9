const express = require("express");
const proxyMiddleware = require("../.proxyrc.js");

var app = express();
proxyMiddleware(app);
app.use(express.static("build-test"));
app.use(express.static("dist"));
app.listen(3001, () => console.log("listening on port 3001"));
