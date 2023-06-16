import React from "react";
import ReactDOM from "react-dom/client";
import { QueryClient, QueryClientProvider } from "react-query";
import * as RestApi from "../components/client";
import {
  OpenK9Client,
  SearchToken,
  OpenK9ClientContext,
} from "../components/client";
import * as RendererComponents from "../renderer-components";
import { Main, QueryState } from "./Main";
import { ResultsDisplayMode } from "../components/ResultList";
import { Tab } from "../components/Tabs";

export const rendererComponents = RendererComponents;

export class OpenK9 {
  static dependencies = {
    React,
    ReactDOM,
    RestApi,
    SearchFrontend: {
      OpenK9,
      rendererComponents,
    },
  };

  private configuration: Configuration;
  /**
   * rest client instance used by this ui
   * @see {@link OpenK9Client} from **@openk9/rest-api** package
   */
  readonly client;
  private root;
  private queryClient;

  constructor(configuration: Partial<Configuration> = {}) {
    this.configuration = { ...defaultConfiguration, ...configuration };
    this.root = ReactDOM.createRoot(this.getRootElement());
    this.queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          refetchOnWindowFocus: false,
          notifyOnChangeProps: ["data", "error"], // for better performance
        },
      },
    });
    this.client = OpenK9Client({
      onAuthenticated: () => {
        this.queryClient.invalidateQueries();
      },
    });
    this.render();
  }

  /** returns current configuration */
  getConfiguration() {
    return { ...this.configuration };
  }

  /**
   * updates configuration with provided fields
   *
   * also accepts a function that produces an object with fields to update
   */
  updateConfiguration: ConfigurationUpdateFunction = (configuration) => {
    if (typeof configuration === "function") {
      this.configuration = {
        ...this.configuration,
        ...configuration(this.configuration),
      };
    } else {
      this.configuration = { ...this.configuration, ...configuration };
    }
    this.notify("configurationChange", this.configuration);
    this.render();
  };

  private render() {
    if (this.configuration.enabled) {
      this.root.render(
        <React.StrictMode>
          <React.Suspense>
            <OpenK9ClientContext.Provider value={this.client}>
              <QueryClientProvider client={this.queryClient}>
                <Main
                  configuration={this.configuration}
                  onConfigurationChange={this.updateConfiguration}
                  onQueryStateChange={this.onQueryStateChange}
                />
              </QueryClientProvider>
            </OpenK9ClientContext.Provider>
          </React.Suspense>
        </React.StrictMode>,
      );
    } else {
      this.root.unmount();
    }
  }

  private getRootElement() {
    const isDevelopment = process.env.NODE_ENV === "development";
    if (!isDevelopment) {
      return document.createDocumentFragment();
    } else {
      // needed for hot-reloading in development
      const win: Window & { ["openk9-react-root"]?: DocumentFragment } =
        window as any;
      if (win["openk9-react-root"]) {
        return win["openk9-react-root"];
      } else {
        win["openk9-react-root"] = document.createDocumentFragment();
        return win["openk9-react-root"];
      }
    }
  }

  /** authenticate the user with given token */
  authenticate = () => {
    return this.client.authenticate();
  };

  /** logs out the user, all subsequent queries will be anonymous */
  deauthenticate = () => {
    return this.client.deauthenticate();
  };

  private listeners: {
    [K in keyof Events]: Set<(payload: Events[K]) => void>;
  } = {
    configurationChange: new Set(),
    queryStateChange: new Set(),
  };
  private notify<E extends keyof Events>(event: E, payload: Events[E]) {
    this.listeners[event].forEach((listener) => listener(payload as any));
  }
  addEventListener<E extends keyof Events>(
    event: E,
    listener: (payload: Events[E]) => void,
  ) {
    this.listeners[event].add(listener as any);
  }
  removeEventListener<E extends keyof Events>(
    event: E,
    listener: (payload: Events[E]) => void,
  ) {
    this.listeners[event].delete(listener as any);
  }

  private onQueryStateChange = (queryState: QueryState) => {
    this.notify("queryStateChange", queryState);
  };
}

export type Configuration = {
  enabled: boolean;
  search: Element | string | null;
  tabs: Element | string | null;
  filters: Element | string | null;
  filtersHorizontal: Element | string | null;
  sortable: Element | string | null;
  results: Element | string | null;
  details: Element | string | null;
  login: Element | string | null;
  detailMobile: Element | string | null;
  searchAutoselect: boolean;
  searchReplaceText: boolean;
  filterTokens: Array<SearchToken>;
  sort: Array<RestApi.SortField>;
  defaultTokens: Array<SearchToken>;
  resultsDisplayMode: ResultsDisplayMode;
  tenant: string | undefined;
  overrideTabs: (tabs: Array<Tab>) => Array<Tab>;
  changeSortResult: (
    sort: Array<RestApi.SortField>,
  ) => Array<RestApi.SortField>;
};

const defaultConfiguration: Configuration = {
  enabled: false,
  search: null,
  tabs: null,
  sortable: null,
  detailMobile: null,
  sort: [],
  filters: null,
  filtersHorizontal: null,
  results: null,
  details: null,
  login: null,
  tenant: undefined,
  searchAutoselect: true,
  searchReplaceText: true,
  filterTokens: [],
  defaultTokens: [],
  resultsDisplayMode: { type: "infinite" },
  overrideTabs: (tabs) => tabs,
  changeSortResult: (sort) => sort,
};

export type ConfigurationUpdateFunction = (
  configuration:
    | Partial<Configuration>
    | ((configuration: Configuration) => Partial<Configuration>),
) => void;

type Events = {
  configurationChange: Configuration;
  queryStateChange: QueryState;
};

window.OpenK9 = OpenK9;
declare global {
  interface Window {
    OpenK9: typeof OpenK9;
  }
}
