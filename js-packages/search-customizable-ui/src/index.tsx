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
import { Main } from "./Main";
import { ThemeProvider } from "react-jss";
import "@clayui/css/lib/css/atlas.css";
import { ClayIconSpriteContext } from "@clayui/icon";
// @ts-ignore
import spritemap from "@clayui/css/lib/images/icons/icons.svg"; // use url:@clayui for `yarn dev` command
import {
  EntityDescription,
  GenericResultItem,
  SearchToken,
} from "../../http-api/src";

const queryClient = new QueryClient();

type OpenK9UIConfiguration = {
  search?: Element | null;
  suggestions?: Element | null;
  tabs?: Element | null;
  results?: Element | null;
  details?: Element | null;
  templates?: OpenK9UITemplates;
};

export type OpenK9UITemplates = {
  tabs?(params: {
    tabs: Array<string>;
    activeIndex: number;
    setActiveIndex(index: number): void;
  }): Element;
  result?(params: {
    result: GenericResultItem<unknown>;
    setDetail(result: GenericResultItem<unknown>): void;
  }): Element | null;
  detail?(params: {
    result: GenericResultItem<unknown>;
  }): Element | null;
  suggestionKind?(params: {
    label: string;
    active: boolean;
    select(): void;
  }): Element;
  suggestionItem?(params: {
    label: string;
    kind: string;
    select(): void;
  }): Element;
  token?(params: {
    token: SearchToken;
    entity: EntityDescription | null;
  }): Element;
  inputPlaceholder?: string;
};

export function initOpenK9({
  search,
  suggestions,
  tabs,
  results,
  details,
  templates,
}: OpenK9UIConfiguration) {
  const root = document.createElement("div");
  document.body.appendChild(root);
  ReactDOM.render(
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={defaultTheme}>
        <ClayIconSpriteContext.Provider value={spritemap}>
          <Suspense fallback={null}>
            <Main templates={templates ?? {}}>
              {(widgets) => (
                <>
                  {search && ReactDOM.createPortal(widgets.search, search)}
                  {suggestions &&
                    ReactDOM.createPortal(widgets.suggestions, suggestions)}
                  {tabs && ReactDOM.createPortal(widgets.tabs, tabs)}
                  {results && ReactDOM.createPortal(widgets.results, results)}
                  {details && ReactDOM.createPortal(widgets.details, details)}
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

declare global {
  interface Window {
    OpenK9: typeof openK9API & {
      deps: ReturnType<typeof loadPluginDepsIntoGlobal>;
    };
  }
}
window.OpenK9 = openK9API;
export default openK9API;

export * from "@openk9/http-api"