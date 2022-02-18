import React from "react";
import ReactDOM from "react-dom";
import { Main } from "./Main";
import { QueryClient, QueryClientProvider } from "react-query";
import { SearchToken } from "@openk9/rest-api";
import * as RestApi from "@openk9/rest-api";
import * as RendererComponents from "../renderer-components";

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
  tabTokens: Array<SearchToken>;
  filterTokens: Array<SearchToken>;
  searchTokens: Array<SearchToken>;
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
  searchAutoselect: boolean;
  searchReplaceText: boolean;
};

const openk9Config: OpenK9ConfigFacade = {
  search: null,
  tabs: null,
  filters: null,
  results: null,
  details: null,
  login: null,
  queryState: {
    tabTokens: [],
    filterTokens: [],
    searchTokens: [],
  },
  searchAutoselect: true,
  searchReplaceText: true,
};

let updateConfig = (targetElements: OpenK9ConfigFacade) => {};

type OpenK9Global = OpenK9ConfigFacade & {
  dependencies: {
    React: typeof React;
    ReactDOM: typeof ReactDOM;
    "@openk9/rest-api": typeof RestApi;
    "@openk9/search-frontend": {
      OpenK9: OpenK9Global;
      rendererComponents: typeof RendererComponents;
    };
  };
};

export const OpenK9: OpenK9Global = {
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
  get queryState() {
    return openk9Config.queryState;
  },
  set searchAutoselect(searchAutoselect: boolean) {
    openk9Config.searchAutoselect = searchAutoselect;
    updateConfig({ ...openk9Config });
  },
  set searchReplaceText(searchReplaceText: boolean) {
    openk9Config.searchReplaceText = searchReplaceText;
    updateConfig({ ...openk9Config });
  },
  dependencies: {
    React,
    ReactDOM,
    "@openk9/rest-api": RestApi,
    "@openk9/search-frontend": {
      get OpenK9() {
        return OpenK9;
      },
      rendererComponents: RendererComponents,
    },
  },
};

export const rendererComponents = RendererComponents;

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
