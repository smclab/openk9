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

import { useCallback, useEffect } from "react";
import create from "zustand";
import { devtools } from "zustand/middleware";
import { useHistory, useParams } from "react-router-dom";

import {
  isSearchQueryEmpty,
  SearchQuery,
  SearchResult,
  doSearch,
  SearchRequest,
  despaceString,
  undespaceString,
  InputSuggestionToken,
  getTokenSuggestions,
  PluginInfo,
  getPlugins,
  TenantJSONConfig,
  emptyTenantJSONConfig,
  getTenants,
  LoginInfo,
  UserInfo,
  doLoginRefresh,
  getUserInfo,
  getTokenInfo,
} from "@openk9/http-api";
import { debounce } from "@openk9/search-ui-components";

const resultsChunkNumber = 8;
const searchTimeoutDebounce = 800;
const suggTimeoutDebounce = 400;
const maxSuggestionsCache = 128;

const localStorageLoginPersistKey = "openk9-login-info";

export type StateType = {
  initial: boolean;
  searchQuery: SearchQuery;
  setSearchQuery(query: SearchQuery): void;
  results: SearchResult<unknown> | null;
  range: [number, number] | null;
  loading: boolean;
  doLoadMore(): void;
  suggestions: InputSuggestionToken[];
  fetchSuggestions(): Promise<void>;
  suggestionsInfo: [number | string, string][];
  suggestionsKind: string | null;
  setSuggestionsKind(suggestionsKind: string | null): void;
  focusToken: number | null;
  setFocusToken(focusToken: number | null): void;
  selectedResult: string | null;
  setSelectedResult(selectedResult: string | null): void;
  pluginInfos: PluginInfo[];
  tenantConfig: TenantJSONConfig;
  loadInitial(): Promise<void>;
  loginInfo: LoginInfo | null;
  userInfo: UserInfo | null;
  setLoginInfo(info: LoginInfo, userInfo: UserInfo): void;
  invalidateLogin(): void;
};

const { loginInfo, userInfo } = JSON.parse(
  localStorage.getItem(localStorageLoginPersistKey) ||
    JSON.stringify({ loginInfo: null, userInfo: null }),
);

export const useStore = create<StateType>(
  devtools((set, get) => ({
    initial: true,
    results: null,
    searchQuery: [],
    loading: false,
    range: null,
    suggestions: [],
    suggestionsKind: null,
    suggestionsInfo: [],
    focusToken: null,
    selectedResult: null,
    pluginInfos: [],
    tenantConfig: emptyTenantJSONConfig,
    loginInfo,
    userInfo,

    async loadInitial() {
      const pluginInfos = await getPlugins(null);

      // TODO getCurrentTenantConfig
      const tenants = await getTenants(null);
      const tenant = tenants.find(
        (tenant) => window.location.host === tenant.virtualHost,
      );
      const tenantConfig =
        (tenant?.jsonConfig && JSON.parse(tenant?.jsonConfig)) ||
        emptyTenantJSONConfig;

      set((state) => ({
        ...state,
        pluginInfos,
        tenantConfig,
      }));
    },

    async setSearchQuery(searchQuery: SearchQuery) {
      set((state) => ({
        ...state,
        searchQuery,
        focusToken:
          searchQuery[state.focusToken || 0] &&
          searchQuery[state.focusToken || 0].tokenType !== "TEXT"
            ? searchQuery.length
            : state.focusToken,
        initial: false,
        loading: true,
      }));

      debounce("suggestionsFetch", get().fetchSuggestions, suggTimeoutDebounce);

      async function performSearch(
        myOpId: number,
        opRef: { lastOpId: number },
      ) {
        const request: SearchRequest = {
          searchQuery,
          range: [0, resultsChunkNumber],
        };
        const [results, suggestions] = await Promise.all([
          doSearch(request, get().loginInfo),

          Promise.all(
            searchQuery.map(async (token) => {
              if (
                (token.tokenType !== "TEXT" || token.keywordKey) &&
                !(
                  get().suggestionsInfo.find(
                    ([k]) =>
                      (token.values as (string | number)[]).indexOf(k) === -1,
                  ) ||
                  get().suggestionsInfo.find(([k]) => k !== token.keywordKey)
                )
              ) {
                const ss = await getTokenInfo(token, get().loginInfo);
                return ss.map((s) => [s.id, s.displayDescription] as const);
              }
            }),
          ),
        ]);

        const filteredSuggestions = suggestions.flat().filter(Boolean) as [
          string,
          string,
        ][];

        if (myOpId === opRef.lastOpId) {
          set((state) => ({
            ...state,
            results: isSearchQueryEmpty(searchQuery) ? null : results,
            loading: false,
            range: [0, resultsChunkNumber],
            suggestionsInfo: [...state.suggestionsInfo, ...filteredSuggestions],
          }));
        }
      }
      debounce("searchFetch", performSearch, searchTimeoutDebounce);
    },

    async doLoadMore() {
      const prev = get();
      if (prev.range) {
        set((state) => ({ ...state, loading: true }));
        const request: SearchRequest = {
          searchQuery: prev.searchQuery,
          range: [prev.range[0], prev.range[1] + resultsChunkNumber],
        };
        const results = await doSearch(request, get().loginInfo);
        set((state) => ({
          ...state,
          results,
          loading: false,
          range: request.range,
        }));
      }
    },

    setFocusToken(focusToken: number | null) {
      set((state) => ({ ...state, focusToken }));
      get().fetchSuggestions();
    },
    setSelectedResult(selectedResult: string | null) {
      set((state) => ({ ...state, selectedResult }));
    },

    // With optional parameters for debouncing
    async fetchSuggestions(myOpId?: number, opRef?: { lastOpId: number }) {
      const { searchQuery, suggestionsKind, loginInfo } = get();

      const suggestions = await getTokenSuggestions(
        searchQuery,
        loginInfo,
        suggestionsKind || undefined,
      );

      const infos = suggestions
        .map((s) => [s.id, s.displayDescription] as [number | string, string])
        .filter(Boolean);

      if (myOpId === undefined || myOpId === opRef?.lastOpId) {
        set((state) => ({
          ...state,
          suggestions,
          suggestionsInfo: [
            ...state.suggestionsInfo
              .slice(0, Math.max(0, maxSuggestionsCache - infos.length))
              .filter(([id]) => !infos.find((s) => s[0] === id)),
            ...infos,
          ],
        }));
      }
    },

    setSuggestionsKind(suggestionsKind: string | null) {
      set((state) => ({ ...state, suggestionsKind }));
      get().fetchSuggestions();
    },

    setLoginInfo(loginInfo: LoginInfo, userInfo: UserInfo) {
      set((state) => ({ ...state, loginInfo, userInfo }));
      (window as any).loginInfo = loginInfo;
      (window as any).userInfo = userInfo;
      localStorage.setItem(
        localStorageLoginPersistKey,
        JSON.stringify({
          loginInfo,
          userInfo,
        }),
      );
    },
    invalidateLogin() {
      set((state) => ({ ...state, loginInfo: null, userInfo: null }));
      (window as any).loginInfo = null;
      (window as any).userInfo = null;
      localStorage.setItem(
        localStorageLoginPersistKey,
        JSON.stringify({
          loginInfo: null,
          userInfo: null,
        }),
      );
    },
  })),
);

export function useSearchQuery() {
  const params = useParams<{ query: string }>();
  const history = useHistory();

  const storedSearchQuery = useStore((s) => s.searchQuery);
  const storedSetSearchQuery = useStore((s) => s.setSearchQuery);

  useEffect(() => {
    try {
      const queryString = undespaceString(params.query || "");
      if (queryString.length > 0) {
        storedSetSearchQuery(JSON.parse(queryString));
      }
    } catch (err) {
      console.warn(err);
    }
  }, [storedSetSearchQuery, params]);

  const setSearchQuery = useCallback(
    (searchQuery: SearchQuery) => {
      storedSetSearchQuery(searchQuery);
      history.push("/q/" + despaceString(JSON.stringify(searchQuery)));
    },
    [history, storedSetSearchQuery],
  );

  return [storedSearchQuery || [], setSearchQuery] as const;
}

export function useLoginInfo() {
  const loginInfo = useStore((s) => s.loginInfo);
  return loginInfo;
}

export function useLoginCheck({ isLoginPage } = { isLoginPage: false }) {
  const history = useHistory();
  const query = new URLSearchParams(window.location.search);

  const loginInfo = useStore((s) => s.loginInfo);
  const userInfo = useStore((s) => s.userInfo);
  const setLoginInfo = useStore((s) => s.setLoginInfo);
  const invalidateLogin = useStore((s) => s.invalidateLogin);
  const tenantConfig = useStore((s) => s.tenantConfig);

  const currentTimeSec = new Date().getTime() / 1000;
  const allowGuest = !tenantConfig.requireLogin;
  const loginValid = loginInfo && userInfo && currentTimeSec < userInfo.exp;
  const isGuest = allowGuest && !loginValid;
  const canEnter = isGuest || loginValid;

  const redirect = query.get("redirect") || "/";

  function goToLogin(redirect?: string) {
    history.push(
      `/login?redirect=${redirect || encodeURIComponent(window.location.href)}`,
    );
  }

  //
  // Login page redirect logic
  //
  useEffect(() => {
    if (isLoginPage && loginValid) {
      // login page and login already done, redirect
      if (decodeURIComponent(redirect).startsWith("/login")) {
        history.push("/");
      } else {
        history.push(decodeURIComponent(redirect));
      }
    } else if (!canEnter && !isLoginPage) {
      // protected page ad no login, redirect to login
      history.push(
        `/login?redirect=${
          redirect || encodeURIComponent(window.location.href)
        }`,
      );
    }
  }, [canEnter, loginValid, isLoginPage, history, redirect]);

  //
  // Refresh loop logic
  //
  useEffect(() => {
    let refreshTimeout: ReturnType<typeof setTimeout> | null;

    async function refreshStep() {
      const { loginInfo } = useStore.getState();

      if (loginInfo) {
        const loginRefresh = await doLoginRefresh({
          refreshToken: loginInfo.refresh_token,
        });
        const userInfo =
          loginRefresh.ok && (await getUserInfo(loginRefresh.response));

        if (loginRefresh.ok && userInfo && userInfo.ok) {
          setLoginInfo(loginRefresh.response, userInfo.response);
        } else {
          invalidateLogin();
        }
      }

      refreshTimeout = setTimeout(
        refreshStep,
        (loginInfo?.expires_in || 1) * 700,
      );
    }

    refreshTimeout = setTimeout(refreshStep, 300);
    return () => {
      if (refreshTimeout) clearTimeout(refreshTimeout);
    };
  }, [invalidateLogin, setLoginInfo]);

  return { loginInfo, userInfo, canEnter, isGuest, goToLogin, loginValid };
}
