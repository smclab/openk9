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
import { useHistory, useLocation, useParams } from "react-router-dom";

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
} from "@openk9/http-api";

const resultsChunkNumber = 8;
const timeoutDebounce = 500;
const suggTimeoutDebounce = 300;

const localStorageLoginPersistKey = "openk9-login-info";

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

export type StateType = {
  initial: boolean;
  searchQuery: SearchQuery;
  setSearchQuery(query: SearchQuery): void;
  lastSearch: number;
  results: SearchResult<{}> | null;
  range: [number, number] | null;
  loading: boolean;
  doLoadMore(): void;
  focus: "INPUT" | "RESULTS";
  suggestionsRequestTime: number;
  suggestions: InputSuggestionToken[];
  setSuggestions(suggestions: InputSuggestionToken[]): void;
  fetchSuggestions(): Promise<void>;
  focusToken: number | null;
  setFocusToken(focusToken: number | null): void;
  setFocus(focus: "INPUT" | "RESULTS"): void;
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
    lastSearch: 0,
    results: null,
    searchQuery: [],
    loading: false,
    range: null,
    focus: "INPUT",
    suggestionsRequestTime: 0,
    suggestions: [],
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
        initial: false,
      }));
      get().fetchSuggestions();

      const startTime = new Date().getTime();
      const lastTime = get().lastSearch;
      set((state) => ({
        ...state,
        lastSearch: startTime,
      }));
      if (startTime - lastTime <= timeoutDebounce) {
        await sleep(timeoutDebounce);
      }
      if (get().lastSearch <= startTime) {
        set((state) => ({
          ...state,
          loading: true,
          lastSearch: startTime,
        }));
        const request: SearchRequest = {
          searchQuery,
          range: [0, resultsChunkNumber],
        };
        const results = await doSearch(request, get().loginInfo);
        set((state) => ({
          ...state,
          results: isSearchQueryEmpty(searchQuery) ? null : results,
          loading: false,
          range: [0, resultsChunkNumber],
        }));
      }
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

    setFocus(focus: "INPUT" | "RESULTS") {
      set((state) => ({ ...state, focus }));
    },
    setSuggestions(suggestions: InputSuggestionToken[]) {
      set((state) => ({ ...state, suggestions }));
    },
    setFocusToken(focusToken: number | null) {
      set((state) => ({ ...state, focusToken }));
      get().fetchSuggestions();
    },
    setSelectedResult(selectedResult: string | null) {
      set((state) => ({ ...state, selectedResult }));
    },

    async fetchSuggestions() {
      const startTime = new Date().getTime();
      const token =
        get().focusToken !== null && get().searchQuery[get().focusToken || 0];
      const lastSuggestionsRequestTime = get().suggestionsRequestTime;
      set((state) => ({ ...state, suggestionsRequestTime: startTime }));
      if (startTime - lastSuggestionsRequestTime <= suggTimeoutDebounce) {
        await sleep(suggTimeoutDebounce);
      }
      if (token && get().suggestionsRequestTime <= startTime) {
        const suggestions = await getTokenSuggestions(token, get().loginInfo);
        if (get().suggestionsRequestTime === startTime) {
          set((state) => ({ ...state, suggestions }));
        }
      } else {
        set((state) => ({ ...state, suggestions: [] }));
      }
    },

    setLoginInfo(loginInfo: LoginInfo, userInfo: UserInfo) {
      set((state) => ({ ...state, loginInfo, userInfo }));
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
  const location = useLocation();
  const query = new URLSearchParams(location.search);

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

  function goToLogin() {
    history.push(`/login?redirect=${encodeURIComponent(location.pathname)}`);
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
      goToLogin();
    }
  }, [canEnter, loginValid, isLoginPage]);

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
  }, []);

  return { loginInfo, userInfo, canEnter, isGuest, goToLogin, loginValid };
}
