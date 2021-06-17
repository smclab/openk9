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

import React, { Suspense } from "react";
import ReactDOM from "react-dom";
import { ThemeProvider } from "react-jss";
import { ClayIconSpriteContext } from "@clayui/icon";

import {
  defaultTheme,
  loadPluginDepsIntoGlobal,
} from "@openk9/search-ui-components";

import { SearchApp } from "./SearchApp";

export function initOpenK9(element: Element) {
  ReactDOM.render(
    <ThemeProvider theme={defaultTheme}>
      <ClayIconSpriteContext.Provider value="/icons.svg">
        <Suspense fallback={null}>
          <SearchApp />
        </Suspense>
      </ClayIconSpriteContext.Provider>
    </ThemeProvider>,
    element,
  );
}

const openK9API = {
  initOpenK9,
  deps: loadPluginDepsIntoGlobal(true),
};

declare global {
  interface Window {
    OpenK9: typeof openK9API & {
      deps: ReturnType<typeof loadPluginDepsIntoGlobal>;
    };
  }
}
window.OpenK9 = openK9API;
export default openK9API;
