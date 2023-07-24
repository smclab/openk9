import React from "react";
import Keycloak from "keycloak-js";
import { useState } from "react";
export const OpenK9ClientContext = React.createContext<
  ReturnType<typeof OpenK9Client>
>(null as any /* must break app if not provided */);

export function useOpenK9Client() {
  return React.useContext(OpenK9ClientContext);
}

declare global {
  interface Window {
    KEYCLOAK_URL: string;
    KEYCLOAK_REALM: string;
    KEYCLOAK_CLIENT_ID: string;
  }
}

export function OpenK9Client({
  onAuthenticated,
  tenant,
  useKeycloack = true,
}: {
  onAuthenticated(): void;
  tenant: string;
  useKeycloack?: boolean;
}) {
  const keycloak = new Keycloak({
    url: window.KEYCLOAK_URL,
    realm: window.KEYCLOAK_REALM || "openk9",
    clientId: window.KEYCLOAK_CLIENT_ID || "openk9",
  });
  const appendToBody = { token: "" };
  const setToken = (newToken: string) => {
    appendToBody.token = newToken;
  };
  const keycloakInit = useKeycloack
    ? keycloak.init({
        onLoad: "check-sso",
        checkLoginIframe: false,
      })
    : null;
  if (keycloakInit)
    keycloakInit.then(() => {
      onAuthenticated();
    });
  async function authFetch(route: string, init: RequestInit = {}) {
    await keycloakInit;
    if (keycloak.authenticated) {
      await keycloak.updateToken(30);
    }
    return fetch(tenant + route, {
      ...init,
      headers: keycloak.authenticated
        ? {
            Authorization: `Bearer ${keycloak.token}`,
            ...init.headers,
          }
        : !useKeycloack
        ? {
            Authorization: `Bearer ${appendToBody.token}`,
            ...init.headers,
          }
        : init.headers,
    });
  }
  return {
    authInit: keycloakInit,
    async authenticate({ token = "" }: { token?: string }) {
      if (useKeycloack) {
        await keycloak.login();
      } else {
        setToken(token || "");
      }
    },
    async deauthenticate() {
      await keycloak.logout();
    },
    async getUserProfile() {
      return await keycloak.loadUserInfo();
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
    }: {
      searchQuery: SearchToken[];
      suggestionCategoryId?: number;
      range?: [number, number]; // for pagination
      afterKey?: string; // for pagination
      suggestKeyword?: string; // to source by text in suggestions
      order: "desc" | "asc";
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
        }),
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      });
      const response = await request.json();
      return response;
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
      Array<{ label: string; tokens: Array<SearchToken> }>
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
      try {
        const jsURL = tenant + `/api/datasource/templates/${id}/compiled`;
        // @ts-ignore

        const code = await import(/* webpackIgnore: true */ jsURL);
        return code.exports.template;
      } catch (err) {
        console.warn(err);
        return null;
      }
    },
    async fetchQueryAnalysis(
      request: AnalysisRequest,
    ): Promise<AnalysisResponse> {
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
      }[];
    };
  } & E;
  highlight: {
    [field in GenericResultItemFields<E>]?: string[];
  };
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
    }
  | {
      tokenType: "DOCTYPE";
      keywordKey: "type";
      values: string[];
      filter: boolean;
      suggestionCategoryId?: number;
      count?: string;
    }
  | {
      tokenType: "TEXT";
      keywordKey?: string;
      values: string[];
      filter: boolean;
      goToSuggestion?: boolean;
      label?: string;
      suggestionCategoryId?: number;
      count?: string;
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
    };

export type SortField = {
  [key: string]: {
    sort: "asc" | "desc";
    missing: "_last";
  };
};

type SearchRequest = {
  searchQuery: Array<SearchToken>;
  range: [number, number];
  sort: SortField[];
};

type SearchResult<E> = {
  result: Array<GenericResultItem<E>>;
  total: number;
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
    };

type SuggestionsCategoriesResult = Array<{
  name: string;
  id: number;
  multiSelect: boolean;
}>;
