import React from "react";
import ReactDOM from "react-dom";
import { Main } from "./Main";
import { QueryClient, QueryClientProvider } from "react-query";
import { SearchToken } from "@openk9/rest-api";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      notifyOnChangeProps: ["data", "error"],
    },
  },
});

function Entry() {
  const [config, setConfig] = React.useState<OpenK9ConfigFacade>(openk9Config);
  React.useLayoutEffect(() => {
    updateConfig = setConfig;
  }, []);
  return (
    <QueryClientProvider client={queryClient}>
      <Main config={config} />;
    </QueryClientProvider>
  );
}

type QueryState = {
  hiddenSearchQuery: Array<SearchToken>;
};

export type OpenK9ConfigFacade = {
  search: Element | null;
  tabs: Element | null;
  filters: Element | null;
  results: Element | null;
  details: Element | null;
  login: Element | null;
  onQueryStateChange?(queryState: QueryState): void;
  queryState: QueryState;
};

const openk9Config: OpenK9ConfigFacade = {
  search: null,
  tabs: null,
  filters: null,
  results: null,
  details: null,
  login: null,
  queryState: {
    hiddenSearchQuery: [],
  },
};

let updateConfig = (targetElements: OpenK9ConfigFacade) => {};

export const OpenK9: OpenK9ConfigFacade = {
  set search(element: Element | null) {
    openk9Config.search = element;
    updateConfig({ ...openk9Config });
  },
  set tabs(element: Element | null) {
    openk9Config.tabs = element;
    updateConfig({ ...openk9Config });
  },
  set filters(element: Element | null) {
    openk9Config.filters = element;
    updateConfig({ ...openk9Config });
  },
  set results(element: Element | null) {
    openk9Config.results = element;
    updateConfig({ ...openk9Config });
  },
  set details(element: Element | null) {
    openk9Config.details = element;
    updateConfig({ ...openk9Config });
  },
  set login(element: Element | null) {
    openk9Config.login = element;
    updateConfig({ ...openk9Config });
  },
  set onQueryStateChange(onQueryStateChange: (queryState: QueryState) => void) {
    openk9Config.onQueryStateChange = onQueryStateChange;
    updateConfig({ ...openk9Config });
  },
  set queryState(queryState: QueryState) {
    openk9Config.queryState = queryState;
    updateConfig({ ...openk9Config });
  },
};

window.OpenK9rootElement =
  window.OpenK9rootElement ?? document.createDocumentFragment();

ReactDOM.render(
  <React.StrictMode>
    <Entry />
  </React.StrictMode>,
  window.OpenK9rootElement,
);

window.OpenK9 = OpenK9;
declare global {
  interface Window {
    OpenK9rootElement: Element;
    OpenK9: typeof OpenK9;
  }
}
