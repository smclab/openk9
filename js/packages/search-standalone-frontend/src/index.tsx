import ReactDOM from "react-dom";
import App from "./App";

import React from "react";
import * as ok9Components from "@openk9/search-ui-components";
import * as ok9API from "@openk9/http-api";
import * as reactJSS from "react-jss";
import clayIcon from "@clayui/icon";

// Load global libraries for plugins
(window as any).React = React;
(window as any).ok9API = ok9API;
(window as any).ok9Components = ok9Components;
(window as any).clayIcon = clayIcon;
(window as any).reactJSS = reactJSS;

ReactDOM.render(<App />, document.getElementById("root"));
