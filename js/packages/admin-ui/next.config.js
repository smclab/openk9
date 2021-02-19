const withTM = require("next-transpile-modules")([
  "@openk9/search-ui-components",
  "@openk9/http-api",
]);
module.exports = {
  ...withTM(),
  basePath: "/admin",
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: "http://dev-projectq.smc.it/api/:path*",
        basePath: false,
      },
      {
        source: "/logs/:path*",
        destination: "http://dev-projectq.smc.it/logs/:path*",
        basePath: false,
      },
    ];
  },
};
