/*
 * Copyright (c) 2021-present SMC Treviso s.r.l. All rights reserved.
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

import { QueryClient, QueryClientProvider } from "react-query";
import {
  loadPluginDepsIntoGlobal,
  defaultTheme,
} from "../../search-ui-components/src";
import { Main } from "./components/Main";
import { ThemeProvider } from "react-jss";
import "@clayui/css/lib/css/atlas.css";
import { ClayIconSpriteContext } from "@clayui/icon";
// @ts-ignore
import spritemap from "@clayui/css/lib/images/icons/icons.svg";
import { OpenK9UIConfiguration } from "./api";

const queryClient = new QueryClient();

export function initOpenK9({
  widgets = {},
  templates = {},
}: OpenK9UIConfiguration) {
  const root = document.createElement("div");
  ReactDOM.render(
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={defaultTheme}>
        <ClayIconSpriteContext.Provider value={spritemap}>
          <Suspense fallback={null}>
            <Main templates={templates}>
              {({ search, suggestions, tabs, results, details }) => (
                <>
                  {widgets.search &&
                    ReactDOM.createPortal(search, widgets.search)}
                  {widgets.suggestions &&
                    ReactDOM.createPortal(suggestions, widgets.suggestions)}
                  {widgets.tabs && ReactDOM.createPortal(tabs, widgets.tabs)}
                  {widgets.results &&
                    ReactDOM.createPortal(results, widgets.results)}
                  {widgets.details &&
                    ReactDOM.createPortal(details, widgets.details)}
                </>
              )}
            </Main>
          </Suspense>
        </ClayIconSpriteContext.Provider>
      </ThemeProvider>
    </QueryClientProvider>,
    root,
  );
}

const openK9API = {
  initOpenK9,
  deps: loadPluginDepsIntoGlobal(true),
};

export default openK9API;

declare global {
  interface Window {
    OpenK9: typeof openK9API & {
      deps: ReturnType<typeof loadPluginDepsIntoGlobal>;
    };
  }
}
window.OpenK9 = openK9API;

export * from "@openk9/http-api";
