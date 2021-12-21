/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

const withTM = require("next-transpile-modules")([
  "@openk9/search-ui-components",
  "@openk9/rest-api",
]);

const isProd = process.env.NODE_ENV === "production";

module.exports = {
  ...withTM(),
  basePath:
    typeof process.env.BASE_PATH == "string" ? process.env.BASE_PATH : "/admin",
  assetPrefix: isProd ? process.env.BASE_PATH_ASSETS : "",
  async rewrites() {
    const basePathProxy = process.env.BASE_PROXY_PATH;
    if (!basePathProxy) return [];
    return [
      {
        source: "/api/:path*",
        destination: `${basePathProxy}/api/:path*`,
        basePath: false,
      },
      {
        source: "/logs/:path*",
        destination: `${basePathProxy}/logs/:path*`,
        basePath: false,
      },
    ];
  },
};
