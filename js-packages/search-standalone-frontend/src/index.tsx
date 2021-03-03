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

import App from "./App";

import React from "react";
import ReactDOM from "react-dom";
import * as ok9Components from "@openk9/search-ui-components";
import * as ok9API from "@openk9/http-api";
import * as reactJSS from "react-jss";
import clayIcon from "@clayui/icon";

// Load global libraries for plugins
(window as any).React = React;
(window as any).ReactDOM = ReactDOM;
(window as any).ok9API = ok9API;
(window as any).ok9Components = ok9Components;
(window as any).clayIcon = clayIcon;
(window as any).reactJSS = reactJSS;

ReactDOM.render(<App />, document.getElementById("root"));
