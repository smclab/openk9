type ReactFC<Props extends Record<string, any>> = (props: Props) => any;

type ClientConfiguration = {
  /**
   * base url for all http requests
   *
   * @example https://demo.openk9.io
   */
  tenant: string;
  /**
   * needed if the tenant configuration is different than base url domain
   */
  tenantDomain?: string;
};

export function OpenK9Client({
  tenant,
  tenantDomain = getDefaultTenantDomain(tenant),
}: ClientConfiguration) {
  let loginInfo: LoginInfo | null = null;

  let refreshTimeoutId: number | null = null;

  let authenticationAction = Promise.resolve();

  function updateLoginInfo(state: AuthenticationState) {
    loginInfo = state?.loginInfo ?? null;
    notify("authenticationStateChange", state);
    if (refreshTimeoutId) {
      clearTimeout(refreshTimeoutId);
      refreshTimeoutId = null;
    }
    if (state && state.loginInfo.emitted_at) {
      refreshTimeoutId = setTimeout(async () => {
        await runAuthenticationAction(async () => {
          try {
            const refreshResponse = await refreshLoginInfo(state.loginInfo);
            if (!refreshResponse.ok) throw new Error();
            const userInfoResponse = await getUserInfo(
              refreshResponse.response,
            );
            if (!userInfoResponse.ok) throw new Error();
            const loginInfo = refreshResponse.response;
            const userInfo = userInfoResponse.response;
            updateLoginInfo({ loginInfo, userInfo });
          } catch (error) {
            console.error(error);
            updateLoginInfo(null);
          }
        });
      }, state.loginInfo.expires_in * 0.5 * 1000 - (Date.now() - state.loginInfo.emitted_at * 1000)) as any;
    }
  }

  async function authFetch(route: string, init: RequestInit = {}) {
    await authenticationAction;
    return fetch(tenant + route, {
      ...init,
      headers: loginInfo
        ? {
            Authorization: `Bearer ${loginInfo.access_token}`,
            ...init.headers,
          }
        : init.headers,
    });
  }

  // used to validate loginInfo, if the call fails, then loginInfo is invalid
  async function getUserInfo(
    loginInfo: LoginInfo,
  ): Promise<{ ok: true; response: UserInfo } | { ok: false; response: any }> {
    try {
      const response = await fetch(`${tenant}/api/searcher/v1/auth/user-info`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${loginInfo.access_token}`,
          "Content-Type": "text/plain",
          Accept: "application/json",
        },
      });
      const data: UserInfo = await response.json();
      return { ok: response.ok, response: data };
    } catch (err) {
      return { ok: false, response: err };
    }
  }

  async function refreshLoginInfo(
    loginInfo: LoginInfo,
  ): Promise<{ ok: true; response: LoginInfo } | { ok: false; response: any }> {
    try {
      const { refresh_token: refreshToken } = loginInfo;
      const response = await fetch(`${tenant}/api/searcher/v1/auth/refresh`, {
        method: "POST",
        body: JSON.stringify({ refreshToken }),
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
      });
      const data = await response.json();
      if (!data.emitted_at) {
        data.emitted_at = Date.now() / 1000;
      }
      return { ok: response.ok, response: data };
    } catch (err) {
      return { ok: false, response: err };
    }
  }

  async function revokeLoginInfo(
    loginInfo: LoginInfo,
  ): Promise<{ ok: boolean; response: any }> {
    try {
      const { access_token: accessToken, refresh_token: refreshToken } =
        loginInfo;
      const request = await authFetch(`${tenant}/api/searcher/v1/auth/logout`, {
        method: "POST",
        body: JSON.stringify({ accessToken, refreshToken }),
        headers: {
          "Content-Type": "application/json",
          Accept: "text/plain",
        },
      });
      const response = await request.text();
      return { ok: request.ok, response };
    } catch (err) {
      return { ok: false, response: err };
    }
  }

  async function runAuthenticationAction<T>(action: () => Promise<T>) {
    await authenticationAction;
    const result = action();
    const noop = () => {};
    authenticationAction = result.then(noop, noop);
    return result;
  }

  const listeners: { [K in keyof Events]: Set<(payload: Events[K]) => void> } =
    {
      authenticationStateChange: new Set(),
    };
  function notify<E extends keyof Events>(event: E, payload: Events[E]) {
    listeners[event].forEach((listener) => listener(payload));
  }

  return {
    get tenant() {
      return tenant;
    },

    /**
     * all subsequent calls to all other methods will be authenticated with the loginInfo provided
     *
     * eventual token refresh will be handled automatically
     */
    async authenticate(loginInfo: LoginInfo) {
      if (!loginInfo.emitted_at) {
        loginInfo.emitted_at = Date.now() / 1000;
      }
      return await runAuthenticationAction(async () => {
        const userInfoResponse = await getUserInfo(loginInfo);
        if (!userInfoResponse.ok) throw new Error();
        const userInfo = userInfoResponse.response;
        updateLoginInfo({ loginInfo, userInfo });
      });
    },

    /**
     * all subsequent calls to all other methods will be UNauthenticated
     *
     * eventual token revocation will be handled automatically
     */
    async deauthenticate() {
      return await runAuthenticationAction(async () => {
        if (loginInfo) {
          revokeLoginInfo(loginInfo);
        }
        updateLoginInfo(null);
      });
    },

    /**
     * used to get loginInfo by username and password with openk9 default authentication
     *
     * it does not authenticate subsequent calls to all other methods
     *
     * @example client.getLoginInfoByUsernamePassword("admin", "admin").then(({response}) => client.authenticate(response))
     */
    async getLoginInfoByUsernamePassword(payload: {
      username: string;
      password: string;
    }): Promise<
      { ok: true; response: LoginInfo } | { ok: false; response: any }
    > {
      try {
        const response = await fetch(`${tenant}/api/searcher/v1/auth/login`, {
          method: "POST",
          body: JSON.stringify(payload),
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
        });
        const data: LoginInfo = await response.json();
        if (!data.emitted_at) {
          data.emitted_at = Date.now() / 1000;
        }
        return { ok: response.ok, response: data };
      } catch (err) {
        return { ok: false, response: err };
      }
    },

    addEventListener<E extends keyof Events>(
      event: E,
      listener: (payload: Events[E]) => void,
    ) {
      listeners[event].add(listener as any);
    },
    removeEventListener<E extends keyof Events>(
      event: E,
      listener: (payload: Events[E]) => void,
    ) {
      listeners[event].delete(listener as any);
    },

    async getServiceStatus(): Promise<"up" | "down"> {
      const response = await fetch(`${tenant}/api/status`);
      if (response.ok) return "up";
      else return "down";
    },

    async doSearch<E>(searchRequest: SearchRequest): Promise<SearchResult<E>> {
      const response = await authFetch(`/v1/search`, {
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

    async doSearchDatasource<E>(
      searchRequest: SearchRequest,
      datasourceId: number,
    ): Promise<SearchResult<E>> {
      const response = await authFetch(
        `/api/searcher/v1/search/${datasourceId}`,
        {
          method: "POST",
          body: JSON.stringify(searchRequest),
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
          },
        },
      );
      const data = await response.json();
      return data;
    },

    async getDataSourceInfo(datasourceId: number): Promise<DataSourceInfo> {
      const response = await authFetch(
        `/api/datasource/v2/datasource/${datasourceId}`,
        {
          headers: {
            Accept: "application/json",
          },
        },
      );
      const data: DataSourceInfo = await response.json();
      return data;
    },

    async postDataSource(
      datasourceInfo: DataSourceInfo,
    ): Promise<DataSourceInfo> {
      const response = await authFetch(`/api/datasource/v2/datasource`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify(datasourceInfo),
      });
      const data: DataSourceInfo = await response.json();
      return data;
    },

    async changeDataSourceInfo(
      datasourceId: number,
      datasource: Partial<DataSourceInfo>,
    ): Promise<DataSourceInfo> {
      const response = await authFetch(
        `/api/datasource/v2/datasource/${datasourceId}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify(datasource),
        },
      );
      const data = await response.json();
      return data;
    },

    async deleteDataSource(datasourceId: number): Promise<string> {
      const response = await authFetch(
        `/api/datasource/v2/datasource/${datasourceId}`,
        {
          method: "DELETE",
          headers: {
            Accept: "text/plain",
          },
        },
      );
      const data = await response.text();
      return data;
    },

    async getDataSources(
      { page, size }: { page: number; size: number } = { page: 0, size: 200 },
    ): Promise<DataSourceInfo[]> {
      const response = await authFetch(
        `/api/datasource/v2/datasource?page=${page}&size=${size}`,
        {
          headers: {
            Accept: "application/json",
          },
        },
      );
      const data = await response.json();
      return data;
    },

    async getEnrichItem(
      { page, size }: { page: number; size: number } = { page: 0, size: 200 },
    ): Promise<EnrichItem[]> {
      const response = await authFetch(
        `/api/datasource/v2/enrichItem?page=${page}&size=${size}`,
        {
          headers: {
            Accept: "application/json",
          },
        },
      );
      const data = await response.json();
      return data;
    },

    async postEnrichItem(
      enrichItem: Omit<EnrichItem, "enrichItemId">,
    ): Promise<EnrichItem> {
      const response = await authFetch(`/api/datasource/v2/enrichItem/`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify(enrichItem),
      });
      const data = await response.json();
      return data;
    },

    async changeEnrichItem(
      enrichItemId: number,
      enrichItem: Partial<EnrichItem>,
    ): Promise<EnrichItem> {
      const response = await authFetch(
        `/api/datasource/v2/enrichItem/${enrichItemId}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify(enrichItem),
        },
      );
      const data: EnrichItem = await response.json();
      return data;
    },

    async deleteEnrichItem(enrichItemId: number): Promise<string> {
      const response = await authFetch(
        `/api/datasource/v2/enrichItem/${enrichItemId}`,
        {
          method: "DELETE",
          headers: {
            Accept: "text/plain",
          },
        },
      );
      const data = await response.text();
      return data;
    },

    async getEnrichPipeline(
      { page, size }: { page: number; size: number } = { page: 0, size: 200 },
    ): Promise<EnrichPipeline[]> {
      const response = await authFetch(
        `/api/datasource/v2/enrichPipeline?page=${page}&size=${size}`,
        {
          headers: {
            Accept: "application/json",
          },
        },
      );
      const data = await response.json();
      return data;
    },

    async postEnrichPipeline(
      enrichPipeline: Omit<EnrichPipeline, "enrichPipelineId">,
    ): Promise<EnrichPipeline> {
      const response = await authFetch(`/api/datasource/v2/enrichPipeline/`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify(enrichPipeline),
      });
      const data: EnrichPipeline = await response.json();
      return data;
    },

    async changeEnrichPipeline(
      enrichPipelineId: number,
      enrichPipeline: Partial<EnrichPipeline>,
    ): Promise<EnrichPipeline[]> {
      const response = await authFetch(
        `/api/datasource/v2/enrichPipeline/${enrichPipelineId}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify(enrichPipeline),
        },
      );
      const data = await response.json();
      return data;
    },

    async deleteEnrichPipeline(enrichPipelineId: number): Promise<string> {
      const response = await authFetch(
        `/api/datasource/v2/enrichPipeline/${enrichPipelineId}`,
        {
          method: "DELETE",
          headers: {
            Accept: "text/plain",
          },
        },
      );
      const data = await response.text();
      return data;
    },

    async reorderEnrichItems(enrichItemsIds: number[]): Promise<string> {
      const response = await authFetch(
        `/api/datasource/v1/enrich-item/reorder`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Accept: "text/plain",
          },
          body: JSON.stringify(enrichItemsIds),
        },
      );
      const data = await response.text();
      return data;
    },

    async triggerScheduler(datasourceIds: Array<number>): Promise<unknown> {
      const response = await authFetch(`/api/datasource/v1/trigger`, {
        method: "POST",
        body: JSON.stringify({ datasourceIds }),
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
      });
      const data = await response.json();
      return data;
    },

    async triggerReindex(ids: number[]): Promise<string> {
      const response = await authFetch(`/api/datasource/v1/index/reindex`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ datasourceIds: ids }),
      });
      const data = await response.text();
      return data;
    },

    async getTenants(): Promise<Tenant[]> {
      const response = await authFetch(`/api/datasource/v2/tenant`, {
        headers: {
          Accept: "application/json",
        },
      });
      const data: Tenant[] = await response.json();
      return data;
    },

    async getTenant(tenantId: number): Promise<Tenant> {
      const response = await authFetch(
        `/api/datasource/v2/tenant/${tenantId}`,
        {
          headers: {
            Accept: "application/json",
          },
        },
      );
      const data = await response.json();
      return data;
    },

    async postTenant(data: {
      name: string;
      virtualHost: string;
      jsonConfig: string;
    }) {
      await authFetch(`/api/datasource/v2/tenant`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });
    },

    async putTenant(data: Tenant): Promise<void> {
      if (!data.jsonConfig) {
        data.jsonConfig = "{}";
      }
      await authFetch(`/api/datasource/v2/tenant/${data.tenantId}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });
    },

    async deleteTenant(tenantId: number) {
      await authFetch(`/api/datasource/v2/tenant/${tenantId}`, {
        method: "DELETE",
      });
    },

    async getTemplatesByVirtualHost(): Promise<Array<string>> {
      const response = await authFetch(`/buckets/current/templates`, {
        headers: {
          Accept: "application/json",
        },
      });
      const data = await response.json();
      return data.map(({ id }: any) => id);
    },

    async loadTemplate<E>(id: string): Promise<Template<E> | null> {
      try {
        const jsURL = `${tenant}/templates/${id}/compiled`;
        // @ts-ignore
        const code = await import(/* webpackIgnore: true */ jsURL);
        return code.template;
      } catch (err) {
        console.warn(err);
        return null;
      }
    },

    async getContainerLogs(id: string, tail = 300): Promise<string> {
      const response = await authFetch(`/api/logs/status/${id}/${tail}`, {
        headers: {
          Accept: "text/plain",
        },
      });
      const data: string = await response.text();
      return data;
    },

    async fetchQueryAnalysis(
      request: AnalysisRequest,
    ): Promise<AnalysisResponse> {
      const response = await authFetch("/v1/query-analysis", {
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
      const request = await authFetch(`/v1/suggestions`, {
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

    async getTenantWithConfiguration() {
      const tenants = await this.getTenants();
      const tenant =
        tenants.find((tenant) => tenant.virtualHost === tenantDomain) ||
        tenants[0];
      const config =
        (tenant?.jsonConfig &&
          (JSON.parse(tenant?.jsonConfig) as TenantJSONConfig)) ||
        {};
      return { tenant, config };
    },

    async getTabsByVirtualHost(): Promise<
      Array<{ label: string; tokens: Array<SearchToken> }>
    > {
      const response = await authFetch(`/buckets/current/tabs`, {
        headers: {
          Accept: "application/json",
        },
      });
      const data = await response.json();
      return data;
    },

    async getSuggestionCategories(): Promise<SuggestionsCategoriesResult> {
      const response = await authFetch(
        `/buckets/current/suggestionCategories`,
        {
          headers: {
            Accept: "application/json",
          },
        },
      );
      const data = await response.json();
      return data;
    },

    async getDatasourceSuggestionCategories(params: {
      page?: number;
      size?: number;
    }): Promise<Array<DatasourceSuggestionCategory>> {
      const response = await authFetch(
        `/api/datasource/v2/suggestion-category?${searchParams(params)}`,
        {
          headers: {
            Accept: "application/json",
          },
        },
      );
      if (!response.ok) {
        throw new Error();
      }
      const data = await response.json();
      return data;
    },

    async createDatasourceSuggestionCategory(params: {
      tenantId: number;
      parentCategoryId: number;
      name: string;
      enabled: true;
      priority: number;
    }): Promise<Array<DatasourceSuggestionCategory>> {
      const response = await authFetch(
        "/api/datasource/v2/suggestion-category",
        {
          method: "POST",
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
          },
          body: JSON.stringify(params),
        },
      );
      if (!response.ok) {
        throw new Error();
      }
      const data = await response.json();
      return data;
    },
    async deleteDatasourceSuggestionCategory(
      suggestionCategoryId: number,
    ): Promise<void> {
      const response = await authFetch(
        `/api/datasource/v2/suggestion-category/${suggestionCategoryId}`,
        {
          method: "DELETE",
        },
      );

      if (!response.ok) {
        throw new Error();
      }
    },

    async updateDatasourceSuggestionCategory(
      suggestionCategoryId: number,
      suggestionCategory: Partial<DatasourceSuggestionCategory>,
    ): Promise<DatasourceSuggestionCategory> {
      const response = await authFetch(
        `/api/datasource/v2/suggestion-category/${suggestionCategoryId}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify(suggestionCategory),
        },
      );
      const data = await response.json();
      return data;
    },

    async getDatasourceSuggestionCategoryFields(params: {
      page?: number;
      size?: number;
    }): Promise<Array<DatasourceSuggestionCategoryField>> {
      const response = await authFetch(
        `/api/datasource/v2/suggestion-category-field?${searchParams(params)}`,
        {
          headers: {
            Accept: "application/json",
          },
        },
      );
      if (!response.ok) {
        throw new Error();
      }
      const data = await response.json();
      return data;
    },

    async createDatasourceSuggestionCategoryField(params: {
      tenantId: number;
      categoryId: number;
      fieldName: string;
      name: string;
      enabled: boolean;
    }): Promise<Array<DatasourceSuggestionCategory>> {
      const response = await authFetch(
        "/api/datasource/v2/suggestion-category-field",
        {
          method: "POST",
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
          },
          body: JSON.stringify(params),
        },
      );

      if (!response.ok) {
        throw new Error();
      }

      const data = await response.json();
      return data;
    },

    async updateDatasourceSuggestionCategoryField(
      suggestionCategoryFieldId: number,
      suggestionCategoryField: Partial<DatasourceSuggestionCategoryField>,
    ): Promise<DatasourceSuggestionCategoryField> {
      const response = await authFetch(
        `/api/datasource/v2/suggestion-category-field/${suggestionCategoryFieldId}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify(suggestionCategoryField),
        },
      );
      const data = await response.json();
      return data;
    },

    async deleteDatasourceSuggestionCategoryField(
      suggestionCategoryFieldId: number,
    ): Promise<void> {
      const response = await authFetch(
        `/api/datasource/v2/suggestion-category-field/${suggestionCategoryFieldId}`,
        {
          method: "DELETE",
        },
      );
      if (!response.ok) {
        throw new Error();
      }
    },

    async getDateFilterFields() {
      const response = await authFetch(`/v1/date-filter`, {
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
  };
}

type Events = {
  /**
   * this function will be called in these cases
   * - the client succesfully authenticates
   * - the client refreshes authentication tokens
   * - the client deauthenticates
   */
  authenticationStateChange: AuthenticationState;
};

export type AuthenticationState = {
  loginInfo: LoginInfo;
  userInfo: UserInfo;
} | null;

/**
 * An authentication token provided by Openk) backend
 */
export type LoginInfo = {
  access_token: string;
  emitted_at: number;
  expires_in: number;
  refresh_token: string;
  refresh_expires_in: number;
  token_type: string;
  "not-before-policy": number;
  session_state: string;
  scope: string;
};

export type UserInfo = {
  exp: number;
  iat: number;
  jti: string;
  iss: string;
  aud: string;
  sub: string;
  typ: string;
  azp: string;
  session_state: string;
  name: string;
  given_name: string;
  family_name: string;
  preferred_username: string;
  email: string;
  email_verified: boolean;
  acr: string;
  realm_access: { [key: string]: string[] };
  resource_access: { [key: string]: { [key: string]: string[] } };
  scope: string;
  client_id: string;
  username: string;
  active: boolean;
};

export type ClientAuthenticationState = {
  loginInfo: LoginInfo;
  userInfo: UserInfo;
} | null;

type SearchRequest = {
  searchQuery: Array<SearchToken>;
  range: [number, number];
};

type SearchResult<E> = {
  result: Array<GenericResultItem<E>>;
  total: number;
};

export type SearchToken =
  | {
      tokenType: "DATASOURCE";
      keywordKey?: undefined;
      values: string[];
      filter: boolean;
    }
  | {
      tokenType: "DOCTYPE";
      keywordKey: "type";
      values: string[];
      filter: boolean;
    }
  | {
      tokenType: "TEXT";
      keywordKey?: string;
      values: string[];
      filter: boolean;
    }
  | {
      tokenType: "ENTITY";
      keywordKey?: string;
      entityType: string;
      entityName: string;
      values: string[];
      filter: boolean;
    }
  | {
      tokenType: "DATE";
      keywordKey?: string;
      extra: {
        gte: number;
        lte: number;
      };
    };

export type GenericResultItem<E = {}> = {
  source: {
    documentTypes: (keyof E)[];
    contentId: string;
    id: string;
    parsingDate: number; // timestamp
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

export type SuggestionResult =
  | {
      tokenType: "DATASOURCE";
      suggestionCategoryId: number;
      value: string;
    }
  | {
      tokenType: "TEXT";
      suggestionCategoryId: number;
      keywordKey?: string;
      value: string;
    }
  | {
      tokenType: "ENTITY";
      suggestionCategoryId: number;
      entityType: string;
      entityValue: string;
      keywordKey?: string;
      value: string;
    }
  | {
      tokenType: "DOCTYPE";
      suggestionCategoryId: number;
      value: string;
    };

export type DataSourceInfo = {
  datasourceId: number;
  active: boolean;
  description: string;
  jsonConfig: string;
  lastIngestionDate: number;
  name: string;
  tenantId: number;
  scheduling: string;
  driverServiceName: string;
};

export type EnrichItem = {
  active: boolean;
  enrichItemId: number;
  enrichPipelineId: number;
  jsonConfig: string;
  name: string;
  serviceName: string;
  _position: number;
};

export type EnrichPipeline = {
  active: boolean;
  datasourceId: number;
  enrichPipelineId: number;
  name: string;
};

export type SchedulerItem = {
  jobName: string;
  datasourceId: number;
  scheduling: string;
  datasourceName: string;
};

export type Tenant = {
  tenantId: number;
  name: string;
  virtualHost: string;
  jsonConfig: string;
};

export type Template<E> = {
  resultType: string;
  priority: number;
  result: ReactFC<ResultRendererProps<E>>;
  detail: ReactFC<DetailRendererProps<E>>;
};

export type ResultRendererProps<E> = {
  result: GenericResultItem<E>;
};

export type DetailRendererProps<E> = {
  result: GenericResultItem<E>;
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

export type AnalysisToken =
  | {
      tokenType: "DOCTYPE";
      value: string;
    }
  | {
      tokenType: "DATASOURCE";
      value: string;
    }
  | {
      tokenType: "ENTITY";
      entityType: string;
      entityName: string;
      keywordKey?: string;
      value: string;
    }
  | {
      tokenType: "TEXT";
      keywordKey?: string;
      keywordName?: string;
      value: string;
    }
  | {
      tokenType: "AUTOCORRECT";
      value: string;
    };

export type EntityDescription = {
  type: string;
  entityId: string;
  name: string;
};
export interface EntityLookupRequest {
  entityId?: string;
  all?: string;
  type?: string;
}
export type EntityLookupResponse = {
  result: EntityDescription[];
  total: number;
};

export type TenantJSONConfig = {
  querySourceBarShortcuts?: { id: string; text: string }[];
  requireLogin?: boolean;
};

type SuggestionsCategoriesResult = Array<{
  name: string;
  id: number;
}>;

export type DatasourceSuggestionCategory = {
  suggestionCategoryId: number;
  tenantId: number;
  parentCategoryId: number;
  name: string;
  enabled: boolean;
  priority: number;
};

export type DatasourceSuggestionCategoryField = {
  suggestionCategoryFieldId: number;
  tenantId: number;
  categoryId: number;
  fieldName: string;
  name: string;
  enabled: boolean;
};

function getDefaultTenantDomain(tenant: string) {
  if (!tenant && typeof window !== "undefined") {
    return window.location.hostname;
  }
  try {
    return new URL(tenant).hostname;
  } catch (error) {
    return "";
  }
}

function searchParams(params: Record<string, string | number | undefined>) {
  return new URLSearchParams(
    Object.fromEntries(
      Object.entries(params).flatMap(([key, value]) =>
        value !== undefined ? ([[key, String(value)]] as const) : [],
      ),
    ),
  );
}
