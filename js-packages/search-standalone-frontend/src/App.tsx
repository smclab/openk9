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

import { Suspense, useLayoutEffect } from "react";
import { ThemeProvider } from "react-jss";
import { ClayIconSpriteContext } from "@clayui/icon";
import { BrowserRouter as Router, Route } from "react-router-dom";

import "@clayui/css/lib/css/base.css";
import "./styles.css";

import { Dockbar, defaultTheme } from "@openk9/search-ui-components";
import { SearchQueryInput } from "./containers/SearchQueryInput";
import { SearchResults } from "./containers/SearchResults";
import { useStore } from "./state";

function App() {
  const loadInitial = useStore((s) => s.loadInitial);
  useLayoutEffect(() => {
    loadInitial();
  }, []);

  return (
    <ThemeProvider theme={defaultTheme}>
      <ClayIconSpriteContext.Provider value="/icons.svg">
        <Router>
          <Dockbar />

          <Route path={["/q/:query", "/"]}>
            <Suspense fallback={<span className="loading-animation" />}>
              <SearchQueryInput />
            </Suspense>
            <Suspense fallback={<span className="loading-animation" />}>
              <SearchResults />
            </Suspense>
          </Route>
        </Router>
      </ClayIconSpriteContext.Provider>
    </ThemeProvider>
  );
}

export default App;
