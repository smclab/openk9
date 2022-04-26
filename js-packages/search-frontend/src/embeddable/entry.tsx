import React from "react";
import ReactDOM from "react-dom";
import { QueryClient, QueryClientProvider } from "react-query";
import * as RestApi from "@openk9/rest-api";
import { OpenK9Client, SearchToken } from "@openk9/rest-api";
import * as RendererComponents from "../renderer-components";
import { OpenK9ClientProvider } from "../components/client";
import { Main, QueryState } from "./Main";

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
    this.root = this.getRoot();
    this.queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          notifyOnChangeProps: ["data", "error"], // for better performance
        },
      },
    });
    this.client = OpenK9Client({ tenant: this.configuration.tenant });
    this.client.addEventListener("authenticationStateChange", () => {
      // invalidate all ched data when user logs in or logs out
      this.queryClient.invalidateQueries();
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
  updateConfiguration = (
    configuration:
      | Partial<MutableConfiguration>
      | ((configuration: Configuration) => Partial<MutableConfiguration>),
  ) => {
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
    ReactDOM.render(
      <React.StrictMode>
        {this.configuration.enabled ? (
          <OpenK9ClientProvider.Provider value={this.client}>
            <QueryClientProvider client={this.queryClient}>
              <Main
                configuration={this.configuration}
                onConfigurationChange={this.updateConfiguration}
                onQueryStateChange={this.onQueryStateChange}
              />
              ;
            </QueryClientProvider>
          </OpenK9ClientProvider.Provider>
        ) : null}
      </React.StrictMode>,
      this.root,
    );
  }

  private getRoot() {
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
  authenticate = (loginInfo: RestApi.LoginInfo) => {
    return this.client.authenticate(loginInfo);
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

export type Configuration = ImmutableConfiguration & MutableConfiguration;

type ImmutableConfiguration = Readonly<{
  tenant: string;
}>;

export type MutableConfiguration = {
  enabled: boolean;
  search: Element | string | null;
  tabs: Element | string | null;
  filters: Element | string | null;
  results: Element | string | null;
  details: Element | string | null;
  login: Element | string | null;
  searchAutoselect: boolean;
  searchReplaceText: boolean;
  filterTokens: Array<SearchToken>;
  defaultTokens: Array<SearchToken>;
};

const defaultConfiguration: Configuration = {
  tenant: "",
  enabled: false,
  search: null,
  tabs: null,
  filters: null,
  results: null,
  details: null,
  login: null,
  searchAutoselect: false,
  searchReplaceText: false,
  filterTokens: [],
  defaultTokens: []
};

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
