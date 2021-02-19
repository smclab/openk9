import { useLayoutEffect } from "react";
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
            <SearchQueryInput />
            <SearchResults />
          </Route>
        </Router>
      </ClayIconSpriteContext.Provider>
    </ThemeProvider>
  );
}

export default App;
