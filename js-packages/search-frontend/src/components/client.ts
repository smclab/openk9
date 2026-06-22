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
import React from "react";
import { UserManager, WebStorageStateStore, type User } from "oidc-client-ts";
import { Options } from "./SortResults";
export const OpenK9ClientContext = React.createContext<
  ReturnType<typeof OpenK9Client>
>(null as any /* must break app if not provided */);

export function useOpenK9Client() {
  return React.useContext(OpenK9ClientContext);
}

const OAUTH2_SETTINGS_ENDPOINT = "/api/datasource/oauth2/settings";

type OauthConfig = {
  issuerUri: string;
  clientId: string;
};

type OauthSettingsResponse = {
  issuerUri?: string | null;
  clientId?: string | null;
  clientSecret?: string | null;
};

let userManager: UserManager | null = null;
let oauth2Enabled = false;
let oauth2InitPromise: Promise<boolean> | null = null;
let cachedAccessToken: string | null = null;

function setCachedAccessToken(token: string | null | undefined) {
  cachedAccessToken = token ?? null;
}

function bindUserManagerEvents(um: UserManager) {
  um.events.addUserLoaded((user) => setCachedAccessToken(user?.access_token));
  um.events.addUserUnloaded(() => setCachedAccessToken(null));
  um.events.addUserSignedOut(() => setCachedAccessToken(null));
  um.events.addAccessTokenExpired(() => setCachedAccessToken(null));
}

async function loadOauthConfig(): Promise<OauthConfig | null> {
  try {
    const res = await fetch(OAUTH2_SETTINGS_ENDPOINT, {
      credentials: "same-origin",
    });
    if (!res.ok) return null;
    const data = (await res.json()) as OauthSettingsResponse;
    if (!data.issuerUri || data.issuerUri === "DISABLED" || !data.clientId) {
      return null;
    }
    return { issuerUri: data.issuerUri, clientId: data.clientId };
  } catch {
    return null;
  }
}

function buildUserManager(config: OauthConfig): UserManager {
  const redirectUri =
    typeof window !== "undefined" ? window.location.origin + window.location.pathname : "";
  return new UserManager({
    authority: config.issuerUri,
    client_id: config.clientId,
    redirect_uri: redirectUri,
    post_logout_redirect_uri: redirectUri,
    response_type: "code",
    scope: "openid",
    automaticSilentRenew: true,
    loadUserInfo: true,
    monitorSession: false,
    userStore: new WebStorageStateStore({ store: window.sessionStorage }),
    stateStore: new WebStorageStateStore({ store: window.sessionStorage }),
  });
}

function isRedirectCallback(): boolean {
  const params = new URLSearchParams(window.location.search);
  return params.has("code") && params.has("state");
}

/**
 * Loads OAuth2 settings and initializes the global UserManager.
 * Resolves to true when authentication is ready (either OAuth2 user available
 * or OAuth2 disabled). Triggers a redirect when an OIDC login is needed and in
 * that case never resolves — the page is leaving.
 */
export function initOAuth2(): Promise<boolean> {
  if (oauth2InitPromise) return oauth2InitPromise;
  oauth2InitPromise = (async () => {
    const config = await loadOauthConfig();
    if (!config) {
      oauth2Enabled = false;
      userManager = null;
      return true;
    }
    oauth2Enabled = true;
    userManager = buildUserManager(config);
    bindUserManagerEvents(userManager);

    try {
      if (isRedirectCallback()) {
        await userManager.signinRedirectCallback();
        window.history.replaceState(
          {},
          document.title,
          window.location.pathname,
        );
      }
      const user = await userManager.getUser();
      if (!user || user.expired) {
        setCachedAccessToken(null);
        return false;
      }
      setCachedAccessToken(user.access_token);
      return true;
    } catch (err) {
      console.error("[auth] OAuth2 init failed", err);
      await userManager.removeUser();
      setCachedAccessToken(null);
      return false;
    }
  })();
  return oauth2InitPromise;
}

export function isOAuth2Enabled(): boolean {
  return oauth2Enabled;
}

export function getUserManager(): UserManager | null {
  return userManager;
}

export function getCachedAccessToken(): string | null {
  return cachedAccessToken;
}

export function OpenK9Client({
  onAuthenticated,
  tenant,
  useOAuth2 = true,
  waitForToken = false,
  callback,
}: {
  onAuthenticated(): void;
  tenant: string;
  useOAuth2?: boolean;
  waitForToken?: boolean;
  callback(): void | null | undefined;
}) {
  // External-token mode (useOAuth2: false): the host application obtains a
  // token (e.g. via an async login) and injects it via openk9.authenticate({token}).
  // When waitForToken is true, authFetch holds requests until the token is set,
  // so calls fired before the host completes its async login are not sent
  // anonymously.
  const EXTERNAL_TOKEN_TIMEOUT_MS = 10_000;
  let externalToken = "";
  let externalTokenWaiters: Array<() => void> = [];
  const setExternalToken = (newToken: string) => {
    externalToken = newToken;
    const waiters = externalTokenWaiters;
    externalTokenWaiters = [];
    waiters.forEach((resolve) => resolve());
  };
  const waitForExternalToken = () =>
    new Promise<void>((resolve) => {
      if (externalToken) {
        resolve();
        return;
      }
      const onSet = () => {
        clearTimeout(timer);
        resolve();
      };
      const timer = setTimeout(() => {
        externalTokenWaiters = externalTokenWaiters.filter((w) => w !== onSet);
        resolve();
      }, EXTERNAL_TOKEN_TIMEOUT_MS);
      externalTokenWaiters.push(onSet);
    });

  const authInit: Promise<boolean> | null = useOAuth2 ? initOAuth2() : null;
  if (authInit) {
    authInit.then((ready) => {
      if (ready) onAuthenticated();
    });
  }

  async function getAccessToken(): Promise<string | null> {
    if (!userManager) return null;
    const user: User | null = await userManager.getUser();
    if (!user || user.expired) return null;
    return user.access_token ?? null;
  }

  async function authFetch(route: string, init: RequestInit = {}) {
    if (callback) callback();

    let headers = init.headers;
    if (useOAuth2 && oauth2Enabled) {
      const token = await getAccessToken();
      if (token) {
        headers = {
          Authorization: `Bearer ${token}`,
          ...init.headers,
        };
      }
    } else if (!useOAuth2) {
      if (waitForToken && !externalToken) {
        await waitForExternalToken();
      }
      if (externalToken) {
        headers = {
          Authorization: `Bearer ${externalToken}`,
          ...init.headers,
        };
      }
    }

    return fetch(tenant + route, {
      ...init,
      headers,
    });
  }
  return {
    authInit,
    async authenticate({ token = "" }: { token?: string }) {
      if (useOAuth2) {
        if (!userManager) await initOAuth2();
        await userManager?.signinRedirect();
      } else {
        setExternalToken(token || "");
      }
    },
    async deauthenticate() {
      if (useOAuth2 && userManager) {
        await userManager.signoutRedirect();
      } else {
        setExternalToken("");
      }
    },
    async getUserProfile() {
      if (useOAuth2 && userManager) {
        const user = await userManager.getUser();
        return user?.profile ?? {};
      }
      return {};
    },
    async getGenerateResponse({
      searchQuery,
      controller,
    }: {
      searchQuery: GenerateRequest;
      controller: AbortController;
    }) {
      const url = "/api/rag/generate";
      const data = await authFetch(url, {
        method: "POST",
        headers: {
          accept: "application/json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify(searchQuery),
        signal: controller.signal,
      });
      return data;
    },
    async getServiceStatus(): Promise<"up" | "down"> {
      const response = await fetch(tenant + `/api/status`);
      if (response.ok) return "up";
      else return "down";
    },
    async doSearch<E>(searchRequest: SearchRequest): Promise<SearchResult<E>> {
      const response = await authFetch(`/api/searcher/v1/search`, {
        method: "POST",
        body: JSON.stringify(searchRequest),
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      });
      const data = await response.json();
      return data;
    },
    async generateResponse<E>(searchRequest: GenerateRequest): Promise<any> {
      const response = await authFetch(`/api/rag/generate`, {
        method: "POST",
        body: JSON.stringify(searchRequest),
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      });
      const data = await response.json();
      return data;
    },
    async getDateFilterFields() {
      const response = await authFetch(`/api/datasource/v1/date-filter`, {
        method: "GET",
        headers: { Accept: "application/json" },
      });
      if (!response.ok) {
        throw new Error();
      }
      const data: Array<{ id: number; field: string; label: string }> =
        await response.json();
      return data;
    },
    async getLabelSort() {
      const response = await authFetch(
        `/api/datasource/buckets/current/doc-type-fields-sortable`,
        {
          method: "GET",
          headers: { Accept: "application/json" },
        },
      );
      if (!response.ok) {
        throw new Error();
      }
      const data: Array<{ field: string; id: number; label: string }> =
        await response.json();
      return data;
    },
    async getLanguages() {
      const response = await authFetch(
        "/api/datasource/buckets/current/availableLanguage",
        {
          method: "GET",
          headers: { Accept: "application/json" },
        },
      );
      if (!response.ok) {
        throw new Error();
      }
      const data: Array<{
        createDate: any;
        modifiedDate: any;
        id: number;
        name: string;
        value: "value";
      }> = await response.json();
      return data;
    },
    async getSortingInfo() {
      const response = await authFetch(
        "/api/datasource/buckets/current/sortings",
        {
          method: "GET",
          headers: { Accept: "application/json" },
        },
      );
      if (!response.ok) {
        throw new Error();
      }
      const data: Array<{
        field: string;
        id: number;
        isDefault: boolean;
        type: string;
        label: string;
        translationMap: {
          "label.en_US"?: string;
          "label.es_ES"?: string;
          "label.it_IT"?: string;
        };
      }> = await response.json();

      return data;
    },
    async getRefreshFilters() {
      const response = await authFetch("/api/datasource/buckets/current", {
        method: "GET",
        headers: { Accept: "application/json" },
      });
      if (!response.ok) {
        throw new Error();
      }
      const data: {
        refreshOnSuggestionCategory: boolean;
        refreshOnTab: boolean;
        refreshOnDate: boolean;
        refreshOnQuery: boolean;
        retrieveType: string;
      } = await response.json();

      return {
        ...data,
        retrieveType: data.retrieveType,
      };
    },
    async getLanguageDefault() {
      const response = await authFetch(
        `/api/datasource/buckets/current/defaultLanguage`,
        {
          method: "GET",
          headers: { Accept: "application/json" },
        },
      );
      if (!response.ok) {
        throw new Error();
      }
      const data: { value: string } = await response.json();
      return data;
    },
    async getSuggestions({
      searchQuery,
      suggestionCategoryId,
      range,
      afterKey,
      suggestKeyword,
      order,
      language,
    }: {
      searchQuery: SearchToken[];
      suggestionCategoryId?: number;
      range?: [number, number]; // for pagination
      afterKey?: string; // for pagination
      suggestKeyword?: string; // to source by text in suggestions
      order: "desc" | "asc";
      language: string;
    }): Promise<{ result: SuggestionResult[]; afterKey: string }> {
      const request = await authFetch(`/api/searcher/v1/suggestions`, {
        method: "POST",
        body: JSON.stringify({
          searchQuery,
          range,
          afterKey,
          suggestionCategoryId,
          suggestKeyword,
          order,
          language,
        }),
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      });
      const response = await request.json();
      return response;
    },
    async getAutocompletes({
      searchText,
    }: {
      searchText: string;
    }): Promise<AutocompleteResponse> {
      const res = await authFetch(`/api/searcher/v1/autocomplete`, {
        method: "POST",
        body: JSON.stringify({ queryText: searchText }),
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      });

      if (!res.ok) {
        const errorText = await res.text().catch(() => "");
        throw new Error(`Autocomplete failed (${res.status}): ${errorText}`);
      }

      const data = (await res.json()) as unknown;

      if (!Array.isArray(data)) {
        throw new Error("Autocomplete response is not an array");
      }

      return data as AutocompleteResponse;
    },

    async handle_dynamic_filters() {
      const response = await authFetch(`/api/datasource/buckets/current`, {
        method: "GET",
        headers: { Accept: "application/json" },
      });
      if (!response.ok) {
        throw new Error();
      }
      const data: { handleDynamicFilters: boolean } = await response.json();
      return data;
    },
    async getSuggestionCategories(): Promise<SuggestionsCategoriesResult> {
      const response = await authFetch(
        `/api/datasource/buckets/current/suggestionCategories`,
        {
          headers: {
            Accept: "application/json",
          },
        },
      );
      const data = await response.json();
      return data;
    },
    async getTabsByVirtualHost(): Promise<
      Array<{
        label: string;
        tokens: Array<SearchToken>;
        translationMap: { [key: string]: string };
        sortings: Options;
      }>
    > {
      const response = await authFetch(`/api/datasource/buckets/current/tabs`, {
        headers: {
          Accept: "application/json",
        },
      });
      const data = await response.json();
      return data;
    },
    async getTemplatesByVirtualHost(): Promise<Array<string>> {
      const response = await authFetch(
        `/api/datasource/buckets/current/templates`,
        {
          headers: {
            Accept: "application/json",
          },
        },
      );
      const data = await response.json();
      return data.map(({ id }: any) => id);
    },
    async loadTemplate<E>(id: string): Promise<Template<E> | null> {
      let blobUrl: string | null = null;
      try {
        const res = await authFetch(`/api/datasource/templates/${id}/compiled`);
        if (!res.ok) throw new Error(String(res.status));
        const src = await res.text();
        const blob = new Blob([src], { type: "text/javascript" });
        blobUrl = URL.createObjectURL(blob);
        // @ts-ignore

        const code = await import(/* @vite-ignore */ /* webpackIgnore: true */ blobUrl);
        return code.exports.template;
      } catch (err) {
        console.warn(err);
        return null;
      } finally {
        if (blobUrl) URL.revokeObjectURL(blobUrl);
      }
    },
    async fetchQueryAnalysis(
      request: AnalysisRequest,
    ): Promise<AnalysisResponse | null> {
      const mock = false;
      const isActiveQueryAnalysis = true;
      if (!isActiveQueryAnalysis) return null;
      if (request.searchText === "") return null;
      if (mock)
        return {
          searchText: "Questo Ã¨ un esempio di testo per l'analisi",
          analysis: [
            {
              text: "Questo Ã¨ un nuovo",
              start: 0,
              end: 5,
              tokens: [
                {
                  label: "",
                  tokenType: "FILTER",
                  value: "ciao",
                  score: 0.8,
                },
              ],
            },
            {
              text: "ancora",
              start: 0,
              end: 5,
              tokens: [
                {
                  label: "test di prova",
                  tokenType: "FILTER",
                  value: "bella",
                  score: 0.8,
                },
              ],
            },
            {
              text: "Questo",
              start: 0,
              end: 5,
              tokens: [
                {
                  label: "",
                  tokenType: "FILTER",
                  value: "prova",
                  keywordKey: "test",
                  score: 0.8,
                },
              ],
            },
          ],
        };
      const response = await authFetch("/api/searcher/v1/query-analysis", {
        method: "POST",
        body: JSON.stringify(request),
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      });
      if (!response.ok) {
        throw new Error();
      }
      const data = await response.json();
      return data;
    },
  };
}

export type GenericResultItem<E = {}> = {
  source: {
    documentTypes: (keyof E)[];
    contentId: string;
    id: string;
    parsingDate: number;
    rawContent: string;
    tenantId: number;
    datasourceId: number;
    entities?: Array<{
      entityType: string;
      context: DeepKeys<Without<GenericResultItem<E>["source"], "entities">>[];
      id: string;
    }>;
    resources: {
      binaries: {
        id: string;
        name: string;
        contentType: string;
        resourceId: string;
      }[];
    };
  } & E;
  highlight: {
    [field in GenericResultItemFields<E>]?: string[];
  };
  sortAfterKey: string;
};

export type GenericResultItemFields<E> = DeepKeys<
  Without<GenericResultItem<E>["source"], "type" | "entities">
>;

type PathImpl<T, Key extends keyof T> = Key extends string
  ? T[Key] extends Record<string, any>
    ?
        | `${Key}.${PathImpl<T[Key], Exclude<keyof T[Key], keyof any[]>> &
            string}`
        | `${Key}.${Exclude<keyof T[Key], keyof any[]> & string}`
    : never
  : never;
type PathImpl2<T> = PathImpl<T, keyof T> | keyof T;
type DeepKeys<T> = PathImpl2<T> extends string | keyof T
  ? PathImpl2<T>
  : keyof T;
type Without<T, K> = Pick<T, Exclude<keyof T, K>>;

export type Template<E> = {
  resultType: string;
  priority: number;
  result: React.FC<ResultRendererProps<E>>;
  detail: React.FC<DetailRendererProps<E>>;
};

export type ResultRendererProps<E> = {
  result: GenericResultItem<E>;
};

export type DetailRendererProps<E> = {
  result: GenericResultItem<E>;
};

export type SearchToken =
  | {
      tokenType: "DATASOURCE";
      keywordKey?: undefined;
      values: string[];
      filter: boolean;
      suggestionCategoryId?: number;
      count?: string;
      isTab?: boolean;
      isFilter?: boolean;
      search?: boolean;
    }
  | {
      tokenType: "DOCTYPE";
      keywordKey: "type";
      values: string[];
      filter: boolean;
      suggestionCategoryId?: number;
      count?: string;
      isTab?: boolean;
      isFilter?: boolean;
      search?: boolean;
    }
  | {
      tokenType: "TEXT";
      keywordKey?: string;
      values: string[];
      filter: boolean;
      goToSuggestion?: boolean;
      overrideSearchWithCorrection?: boolean;
      label?: string;
      suggestionCategoryId?: number;
      count?: string;
      isTab?: boolean;
      isFilter?: boolean;
      search?: boolean;
      extra?: {
        boost?: string;
        fuzziness?: string;
      };
    }
  | {
      tokenType: "ENTITY";
      keywordKey?: string;
      entityType: string;
      entityName: string;
      values: string[];
      filter: boolean;
      suggestionCategoryId?: number;
      count?: string;
      isTab?: boolean;
      isFilter?: boolean;
      search?: boolean;
    }
  | {
      tokenType: "DATE";
      keywordKey?: string;
      extra: {
        gte: number;
        lte: number;
      };
      suggestionCategoryId?: number;
      count?: string;
      values?: string[];
      isTab?: boolean;
      isFilter?: boolean;
      search?: boolean;
    }
  | {
      tokenType: "FILTER";
      keywordKey?: string;
      values: string[];
      filter: boolean;
      goToSuggestion?: boolean;
      label?: string;
      suggestionCategoryId?: number;
      count?: string;
      isTab?: boolean;
      isFilter?: boolean;
      search?: boolean;
    };

export type SortField = {
  [key: string]: {
    sort: "asc" | "desc";
    missing: "_last";
  };
};

export type AutocompleteResponseWrapped = {
  items: AutocompleteSuggestion[];
};

export type AutocompleteSuggestion = {
  autocomplete: string;
  labelDocType: string;
};

export type AutocompleteResponse = AutocompleteSuggestion[];

export type AutocompleteRequest = {
  queryText: string;
};

export type AutocompleteEsQuery = {
  _source: {
    includes: Array<"web.title" | "web.productName" | string>;
  };
  query: {
    multi_match: {
      fields: Array<
        "web.title.searchasyou" | "web.productName.searchasyou" | string
      >;
      fuzziness: "AUTO" | number | string;
      minimum_should_match: string;
      operator: "or" | "and";
      query: string;
      type: "bool_prefix";
    };
  };
  size: number;
  sort: Array<{
    _score: {
      order: "asc" | "desc";
    };
  }>;
};

type SearchRequest = {
  searchQuery: Array<SearchToken>;
  range: [number, number];
  sort: SortField[];
  language: string;
  sortAfterKey: string;
  overrideSearchWithCorrection?: boolean;
};

export type GenerateRequest = {
  searchQuery: Array<SearchToken>;
  range: [number, number];
  sort: SortField[];
  language: string;
  sortAfterKey: string;
  searchText: string;
};

type SearchResult<E> = {
  result: Array<GenericResultItem<E>>;
  total: number;
  autocorrection: AutocorrectionType;
};

export type AutocorrectionType = {
  originalText: string;
  searchedWithCorrectedText: boolean;
  autocorrectionText: string;
  suggestions: [
    { offset: number; length: number; text: string; correction: string },
  ];
};

export type AnalysisToken =
  | {
      tokenType: "DOCTYPE";
      value: string;
      label: string;
    }
  | {
      tokenType: "DATASOURCE";
      value: string;
      label: string;
    }
  | {
      tokenType: "ENTITY";
      entityType: string;
      entityName: string;
      keywordKey?: string;
      value: string;
      label: string;
    }
  | {
      tokenType: "TEXT";
      keywordKey?: string;
      keywordName?: string;
      value: string;
      label: string;
      extra?: {
        boost?: string;
        fuzziness?: string;
      };
    }
  | {
      tokenType: "AUTOCORRECT";
      value: string;
      label: string;
    }
  | {
      tokenType: "AUTOCOMPLETE";
      value: string;
      label: string;
    }
  | {
      tokenType: "FILTER";
      keywordKey?: string;
      keywordName?: string;
      value: string;
      label: string;
    };

export type AnalysisRequest = {
  searchText: string;
  tokens: Array<AnalysisRequestEntry>;
};

export type AnalysisRequestEntry = {
  text: string;
  start: number;
  end: number;
  token: AnalysisToken;
};

export type AnalysisResponse = {
  searchText: string;
  analysis: Array<AnalysisResponseEntry>;
};

export type AnalysisResponseEntry = {
  text: string;
  start: number;
  end: number;
  tokens: Array<
    AnalysisToken & {
      score: number; // 0 - 1
    }
  >;
};

export type SuggestionResult =
  | {
      tokenType: "DATASOURCE";
      suggestionCategoryId: number;
      value: string;
      keywordKey?: string;
      count?: string;
    }
  | {
      tokenType: "TEXT";
      suggestionCategoryId: number;
      keywordKey?: string;
      value: string;
      count?: string;
    }
  | {
      tokenType: "ENTITY";
      suggestionCategoryId: number;
      entityType: string;
      entityValue: string;
      keywordKey?: string;
      value: string;
      count?: string;
    }
  | {
      tokenType: "DOCTYPE";
      suggestionCategoryId: number;
      value: string;
      keywordKey?: string;
      count?: string;
    }
  | {
      tokenType: "FILTER";
      suggestionCategoryId: number;
      keywordKey?: string;
      value: string;
      count?: string;
    };

type SuggestionsCategoriesResult = Array<{
  name: string;
  id: number;
  multiSelect: boolean;
  translationMap: { [key: string]: string };
}>;
