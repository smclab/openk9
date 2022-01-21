export type LoginInfo = {
  access_token: string;
  expires_in: number;
  refresh_expires_in: number;
  refresh_token: string;
  token_type: string;
  "not-before-policy": number;
  session_state: string;
  scope: string;
};

function authFetch(
  input: RequestInfo,
  loginInfo: LoginInfo | null,
  init: RequestInit = {},
) {
  return fetch(input, {
    ...init,
    headers: loginInfo
      ? {
          Authorization: `Bearer ${loginInfo.access_token}`,
          ...init.headers,
        }
      : init.headers,
  });
}

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

// TODO better
export async function getUserInfo(
  loginInfo: LoginInfo,
): Promise<{ ok: true; response: UserInfo } | { ok: false; response: any }> {
  try {
    const response = await authFetch(
      `/api/searcher/v1/auth/user-info`,
      loginInfo,
      {
        method: "POST",
        body: "",
        headers: {
          "Content-Type": "text/plain",
          Accept: "application/json",
        },
      },
    );
    const data: UserInfo = await response.json();
    return { ok: response.ok, response: data };
  } catch (err) {
    return { ok: false, response: err };
  }
}

const AUTH_TIMEOUT = 6000;

export async function doLogin(
  payload: {
    username: string;
    password: string;
  },
  timeout = AUTH_TIMEOUT,
): Promise<{ ok: true; response: LoginInfo } | { ok: false; response: any }> {
  async function innerLogin() {
    const response = await fetch(`/api/searcher/v1/auth/login`, {
      method: "POST",
      body: JSON.stringify(payload),
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
    });
    return [response, await response.json()] as const;
  }

  try {
    const [response, data] = await promiseTimeoutReject(innerLogin(), timeout);
    return { ok: response.ok, response: data };
  } catch (err) {
    return { ok: false, response: err };
  }
}

export async function doLoginRefresh(
  payload: {
    refreshToken: string;
  },
  timeout = AUTH_TIMEOUT,
): Promise<{ ok: true; response: LoginInfo } | { ok: false; response: any }> {
  async function innerRefresh() {
    const response = await fetch(`/api/searcher/v1/auth/refresh`, {
      method: "POST",
      body: JSON.stringify(payload),
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
    });
    return [response, await response.json()] as const;
  }

  try {
    const [response, data] = await promiseTimeoutReject(
      innerRefresh(),
      timeout,
    );
    return { ok: response.ok, response: data };
  } catch (err) {
    return { ok: false, response: err };
  }
}

function abortTimeout<T>(ms: number) {
  return new Promise<T>((_, reject) => setTimeout(() => reject("timeout"), ms));
}

function promiseTimeoutReject<T>(promise: Promise<T>, ms: number) {
  return Promise.race([promise, abortTimeout<T>(ms)]);
}

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
    }
  | {
      tokenType: "DOCTYPE";
      keywordKey: "type";
      values: string[];
    }
  | {
      tokenType: "TEXT";
      keywordKey?: string;
      values: string[];
    }
  | {
      tokenType: "ENTITY";
      keywordKey?: string;
      entityType: string;
      values: string[];
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

export async function doSearch<E>(
  searchRequest: SearchRequest,
  loginInfo: LoginInfo | null,
): Promise<SearchResult<E>> {
  const response = await authFetch(`/api/searcher/v1/search`, loginInfo, {
    method: "POST",
    body: JSON.stringify(searchRequest),
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
  });
  const data = await response.json();
  return data;
}

export async function doSearchDatasource<E>(
  searchRequest: SearchRequest,
  datasourceId: number,
  loginInfo: LoginInfo | null,
): Promise<SearchResult<E>> {
  const response = await authFetch(
    `/api/searcher/v1/search/${datasourceId}`,
    loginInfo,
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
}

// TODO: remove
export async function getItemsInDatasource<E>(
  datasourceId: number,
  loginInfo: LoginInfo | null,
): Promise<SearchResult<E>> {
  return doSearchDatasource(
    { range: [0, 0], searchQuery: [] },
    datasourceId,
    loginInfo,
  );
}

// DEPRECATED
// TODO: remove
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

export async function getDataSourceInfo(
  datasourceId: number,
  loginInfo: LoginInfo | null,
): Promise<DataSourceInfo> {
  const response = await authFetch(
    `/api/datasource/v2/datasource/${datasourceId}`,
    loginInfo,
    {
      headers: {
        Accept: "application/json",
      },
    },
  );
  const data: DataSourceInfo = await response.json();
  return data;
}

export async function postDataSource(
  datasourceInfo: DataSourceInfo,
  loginInfo: LoginInfo | null,
): Promise<DataSourceInfo> {
  const response = await authFetch(`/api/datasource/v2/datasource`, loginInfo, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify(datasourceInfo),
  });
  const data: DataSourceInfo = await response.json();
  return data;
}

export async function changeDataSourceInfo(
  datasourceId: number,
  datasource: Partial<DataSourceInfo>,
  loginInfo: LoginInfo | null,
): Promise<DataSourceInfo> {
  const response = await authFetch(
    `/api/datasource/v2/datasource/${datasourceId}`,
    loginInfo,
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
}

export async function deleteDataSource(
  datasourceId: number,
  loginInfo: LoginInfo | null,
): Promise<string> {
  const response = await authFetch(
    `/api/datasource/v2/datasource/${datasourceId}`,
    loginInfo,
    {
      method: "DELETE",
      headers: {
        Accept: "text/plain",
      },
    },
  );
  const data = await response.text();
  return data;
}

export async function getDataSources(
  loginInfo: LoginInfo | null,
): Promise<DataSourceInfo[]> {
  const response = await authFetch(`/api/datasource/v2/datasource`, loginInfo, {
    headers: {
      Accept: "application/json",
    },
  });
  const data = await response.json();
  return data;
}

// TODO: remove
export async function toggleDataSource(
  datasourceId: number,
  loginInfo: LoginInfo | null,
  active: boolean,
): Promise<DataSourceInfo> {
  return changeDataSourceInfo(datasourceId, { active }, loginInfo);
}

export type EnrichItem = {
  active: boolean;
  enrichItemId: number;
  enrichPipelineId: number;
  jsonConfig: string;
  name: string;
  serviceName: string;
  _position: number;
};

export async function getEnrichItem(
  loginInfo: LoginInfo | null,
): Promise<EnrichItem[]> {
  const response = await authFetch(`/api/datasource/v2/enrichItem`, loginInfo, {
    headers: {
      Accept: "application/json",
    },
  });
  const data = await response.json();
  return data;
}

export async function postEnrichItem(
  enrichItem: Omit<EnrichItem, "enrichItemId">,
  loginInfo: LoginInfo | null,
): Promise<EnrichItem> {
  const response = await authFetch(
    `/api/datasource/v2/enrichItem/`,
    loginInfo,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      body: JSON.stringify(enrichItem),
    },
  );
  const data = await response.json();
  return data;
}

export async function changeEnrichItem(
  enrichItemId: number,
  enrichItem: Partial<EnrichItem>,
  loginInfo: LoginInfo | null,
): Promise<EnrichItem> {
  const response = await authFetch(
    `/api/datasource/v2/enrichItem/${enrichItemId}`,
    loginInfo,
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
}

export async function deleteEnrichItem(
  enrichItemId: number,
  loginInfo: LoginInfo | null,
): Promise<string> {
  const response = await authFetch(
    `/api/datasource/v2/enrichItem/${enrichItemId}`,
    loginInfo,
    {
      method: "DELETE",
      headers: {
        Accept: "text/plain",
      },
    },
  );
  const data = await response.text();
  return data;
}

export type EnrichPipeline = {
  active: boolean;
  datasourceId: number;
  enrichPipelineId: number;
  name: string;
};

export async function getEnrichPipeline(
  loginInfo: LoginInfo | null,
): Promise<EnrichPipeline[]> {
  const response = await authFetch(
    `/api/datasource/v2/enrichPipeline`,
    loginInfo,
    {
      headers: {
        Accept: "application/json",
      },
    },
  );
  const data = await response.json();
  return data;
}

export async function postEnrichPipeline(
  enrichPipeline: Omit<EnrichPipeline, "enrichPipelineId">,
  loginInfo: LoginInfo | null,
): Promise<EnrichPipeline> {
  const response = await authFetch(
    `/api/datasource/v2/enrichPipeline/`,
    loginInfo,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      body: JSON.stringify(enrichPipeline),
    },
  );
  const data: EnrichPipeline = await response.json();
  return data;
}

export async function changeEnrichPipeline(
  enrichPipelineId: number,
  enrichPipeline: Partial<EnrichPipeline>,
  loginInfo: LoginInfo | null,
): Promise<EnrichPipeline[]> {
  const response = await authFetch(
    `/api/datasource/v2/enrichPipeline/${enrichPipelineId}`,
    loginInfo,
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
}

export async function deleteEnrichPipeline(
  enrichPipelineId: number,
  loginInfo: LoginInfo | null,
): Promise<string> {
  const response = await authFetch(
    `/api/datasource/v2/enrichPipeline/${enrichPipelineId}`,
    loginInfo,
    {
      method: "DELETE",
      headers: {
        Accept: "text/plain",
      },
    },
  );
  const data = await response.text();
  return data;
}

export async function reorderEnrichItems(
  enrichItemsIds: number[],
  loginInfo: LoginInfo | null,
): Promise<string> {
  const response = await authFetch(
    `/api/datasource/v1/enrich-item/reorder`,
    loginInfo,
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
}

export async function triggerScheduler(
  schedulerJobs: string[],
  loginInfo: LoginInfo | null,
): Promise<
  {
    errors: string[];
  } & { [key: string]: boolean }
> {
  const response = await authFetch(
    `/api/datasource/v1/scheduler/trigger`,
    loginInfo,
    {
      method: "POST",
      body: JSON.stringify(schedulerJobs),
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
    },
  );
  const data = await response.json();
  return data;
}

export async function triggerReindex(
  ids: number[],
  loginInfo: LoginInfo | null,
): Promise<string> {
  const response = await authFetch(
    `/api/datasource/v1/index/reindex`,
    loginInfo,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "text/plain",
      },
      body: JSON.stringify({ datasourceIds: ids }),
    },
  );
  const data = await response.text();
  return data;
}

export type SchedulerItem = {
  jobName: string;
  datasourceId: number;
  scheduling: string;
  datasourceName: string;
};

export async function getSchedulerItems(
  loginInfo: LoginInfo | null,
): Promise<SchedulerItem[]> {
  const response = await authFetch(`/api/datasource/v1/scheduler`, loginInfo);
  const data = await response.json();
  return data;
}

export type Tenant = {
  tenantId: number;
  name: string;
  virtualHost: string;
  jsonConfig: string;
};

export async function getTenants(
  loginInfo: LoginInfo | null,
): Promise<Tenant[]> {
  const response = await authFetch(`/api/datasource/v2/tenant`, loginInfo, {
    headers: {
      Accept: "application/json",
    },
  });
  const data: Tenant[] = await response.json();
  return data;
}

export async function getTenant(
  tenantId: number,
  loginInfo: LoginInfo | null,
): Promise<Tenant> {
  const response = await authFetch(
    `/api/datasource/v2/tenant/${tenantId}`,
    loginInfo,
    {
      headers: {
        Accept: "application/json",
      },
    },
  );
  const data = await response.json();
  return data;
}

export async function postTenant(
  data: {
    name: string;
    virtualHost: string;
    jsonConfig: string;
  },
  loginInfo: LoginInfo | null,
) {
  await authFetch(`/api/datasource/v2/tenant`, loginInfo, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
}

export async function putTenant(
  data: Tenant,
  loginInfo: LoginInfo | null,
): Promise<void> {
  if (!data.jsonConfig) {
    data.jsonConfig = "{}";
  }
  await authFetch(`/api/datasource/v2/tenant`, loginInfo, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
}

export async function deleteTenant(
  tenantId: number,
  loginInfo: LoginInfo | null,
) {
  await authFetch(`/api/datasource/v2/tenant/${tenantId}`, loginInfo, {
    method: "DELETE",
  });
}

export type Plugin<E> = {
  pluginId: string;
  displayName: string;
  pluginServices: PluginService<E>[];
};

export type PluginInfo = {
  pluginId: string;
  bundleInfo: {
    id: number;
    lastModified: number;
    state: string;
    symbolicName: string;
    version: string;
  };
};

export type PluginService<E> =
  | DataSourcePlugin
  | EnrichPlugin
  | SuggestionsPlugin
  | ResultRendererPlugin<E>;

export type DataSourcePlugin = {
  type: "DATASOURCE";
  displayName: string;
  driverServiceName: string;
  iconRenderer?: React.FC<{ size?: number } & any>;
  initialSettings: string;
  settingsRenderer?: React.FC<{
    currentSettings: string;
    setCurrentSettings(a: string): void;
  }>;
};

export type EnrichPlugin = {
  type: "ENRICH";
  displayName: string;
  serviceName: string;
  iconRenderer?: React.FC<{ size?: number } & any>;
  initialSettings: string;
  settingsRenderer?: React.FC<{
    currentSettings: string;
    setCurrentSettings(a: string): void;
  }>;
};

export type SuggestionsPlugin = {
  type: "SUGGESTIONS";
  renderSuggestionIcons?: React.FC<{ suggestion: SuggestionResult }>;
};

export type ResultRendererPlugin<E> = {
  type: "RESULT_RENDERER";
  priority?: number;
  resultType: string;
  resultRenderer: React.FC<ResultRendererProps<E>>;
  sidebarRenderer: React.FC<SidebarRendererProps<E>>;
};

export type ResultRendererProps<E> = {
  data: GenericResultItem<E>;
  onSelect(): void;
  loginInfo: LoginInfo | null;
};

export type SidebarRendererProps<E> = {
  result: GenericResultItem<E>;
  loginInfo: LoginInfo | null;
};

export async function getPlugins(
  loginInfo: LoginInfo | null,
): Promise<PluginInfo[]> {
  const response = await authFetch(
    `/api/plugin-driver-manager/v1/plugin`,
    loginInfo,
    {
      headers: {
        Accept: "application/json",
      },
    },
  );
  const data = await response.json();
  return data;
}

export async function loadPlugin<E>(
  id: string,
  lastModified: number,
): Promise<Plugin<E>> {
  const defaultPlugin: Plugin<any> = {
    pluginId: id,
    displayName: id,
    pluginServices: [],
  };

  try {
    const jsURL = `/api/plugin-driver-manager/plugins/${id}/static/build/index.js?t=${lastModified}`;
    // @ts-ignore
    const code = await import(/* webpackIgnore: true */ jsURL);
    const plugin = code.plugin as Plugin<E>;
    return plugin;
  } catch (err) {
    console.warn(err);
    return defaultPlugin;
  }
}

// TODO remove
export function getServices<E>(plugins: Plugin<E>[]) {
  return plugins.flatMap((p) =>
    p.pluginServices.map((ps) => ({ ...ps, pluginId: p.pluginId })),
  );
}

export type ContainerStatus = {
  ID: string;
  Image: string;
  Names: string;
  Status: string;
};

export async function getContainerStatus(
  loginInfo: LoginInfo | null,
): Promise<ContainerStatus[]> {
  const response = await authFetch(`/api/logs/status`, loginInfo, {
    headers: {
      Accept: "application/json",
    },
  });
  const data: ContainerStatus[] = await response.json();
  return data;
}

export async function getContainerLogs(
  id: string,
  tail = 300,
  loginInfo: LoginInfo | null,
): Promise<string> {
  const response = await authFetch(
    `/api/logs/status/${id}/${tail}`,
    loginInfo,
    {
      headers: {
        Accept: "text/plain",
      },
    },
  );
  const data: string = await response.text();
  return data;
}

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
      value: string;
    };

export async function fetchQueryAnalysis(
  request: AnalysisRequest,
  loginInfo: LoginInfo | null,
): Promise<AnalysisResponse> {
  const response = await authFetch(
    "/api/searcher/v1/query-analysis",
    loginInfo,
    {
      method: "POST",
      body: JSON.stringify(request),
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    },
  );
  const data = await response.json();
  return data;
}

// DEPRECATED
// TODO: remove
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

// DEPRECATED
// TODO: remove
export async function getSuggestions({
  searchQuery,
  suggestionCategoryId,
  range,
  afterKey,
  loginInfo,
}: {
  searchQuery: SearchToken[];
  suggestionCategoryId?: number;
  range?: [number, number]; // for pagination
  afterKey?: string; // for pagination
  loginInfo: LoginInfo | null;
}): Promise<{ result: SuggestionResult[]; afterKey: string }> {
  const request = await authFetch(`/api/searcher/v1/suggestions`, loginInfo, {
    method: "POST",
    body: JSON.stringify({
      searchQuery,
      range,
      afterKey,
      suggestionCategoryId,
    }),
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
  });
  const response = await request.json();
  return response;
}

const tenantDomain =
  window.location.hostname === "localhost"
    ? prompt("tentant domain")
    : window.location.host;

export async function getTentantWithConfiguration(loginInfo: LoginInfo | null) {
  const tenants = await getTenants(loginInfo);
  const tenant =
    tenants.find((tenant) => tenant.virtualHost === tenantDomain) || tenants[0];
  const config =
    (tenant?.jsonConfig &&
      (JSON.parse(tenant?.jsonConfig) as TenantJSONConfig)) ||
    {};
  return { tenant, config };
}

export type TenantJSONConfig = {
  querySourceBarShortcuts?: { id: string; text: string }[];
  requireLogin?: boolean;
};

// DEPRECATED
// TODO: remove
export function readQueryParamToken(
  query: SearchToken[],
  keywordKey: string,
): (string | number)[] | null {
  const item = query.find((i) => i.keywordKey === keywordKey);
  if (item) {
    return item.values;
  } else {
    return null;
  }
}

// DEPRECATED
// TODO: remove
export function setQueryParamToken({
  query,
  keywordKey,
  values,
  entityType,
  tokenType,
}: {
  query: SearchToken[];
  keywordKey: string;
  values: string[] | number[] | null;
  tokenType: SearchToken["tokenType"];
  entityType?: string;
}): SearchToken[] {
  if (values === null) {
    return query.filter((i) => i.keywordKey !== keywordKey);
  } else if (query.find((i) => i.keywordKey === keywordKey)) {
    return query.map((i) =>
      i.keywordKey === keywordKey &&
      (i.tokenType !== "ENTITY" || i.entityType === entityType) &&
      i.tokenType === tokenType
        ? { ...i, values: values as any }
        : i,
    );
  } else {
    const item: any = {
      tokenType,
      keywordKey,
      values,
      entityType,
    };
    // @ts-ignore
    return [...query, item];
  }
}

// DEPRECATED
// TODO: remove
export const ROOT_SUGGESTION_CATEGORY_ID = -1;
export const ALL_SUGGESTION_CATEGORY_ID = 100000;
export const KEYWORDS_SUGGESTION_CATEGORY_ID = 99999;

// DEPRECATED
// TODO: remove
export async function doSearchEntities(
  query: EntityLookupRequest,
  loginInfo: LoginInfo | null,
): Promise<EntityLookupResponse> {
  const request = await authFetch(`/api/searcher/v1/entity`, loginInfo, {
    method: "POST",
    body: JSON.stringify(query),
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
  });
  return await request.json();
}

// DEPRECATED
// TODO: remove
export async function getSuggestionCategories(
  loginInfo: LoginInfo | null,
): Promise<SuggestionsCategoriesResult> {
  const response = await authFetch(
    `/api/searcher/suggestion-categories`,
    loginInfo,
    {
      headers: {
        Accept: "application/json",
      },
    },
  );
  const data = (await response.json()) as SuggestionsCategoriesResult;
  return [
    {
      name: "All",
      parentCategoryId: ROOT_SUGGESTION_CATEGORY_ID,
      suggestionCategoryId: ALL_SUGGESTION_CATEGORY_ID,
      tenantId: NaN,
    },
    {
      name: "Keywords",
      parentCategoryId: ROOT_SUGGESTION_CATEGORY_ID,
      suggestionCategoryId: KEYWORDS_SUGGESTION_CATEGORY_ID,
      tenantId: NaN,
    },
    ...data,
  ];
}

// DEPRECATED
// TODO: remove
type SuggestionsCategoriesResult = Array<{
  name: string;
  parentCategoryId: number;
  suggestionCategoryId: number;
  tenantId: number;
}>;

// DEPRECATED
// TODO: remove
export async function getDocumentTypes(
  loginInfo: LoginInfo | null,
): Promise<Record<string, Array<string>>> {
  const request = await authFetch(
    `/api/searcher/v1/document-types`,
    loginInfo,
    {
      headers: {
        Accept: "application/json",
      },
    },
  );
  const response = await request.json();
  return response;
}
