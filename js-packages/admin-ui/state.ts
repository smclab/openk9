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

import create from "zustand";
import { devtools, persist } from "zustand/middleware";
import { useEffect } from "react";
import { useRouter } from "next/router";
import {
  doLoginRefresh,
  getUserInfo,
  LoginInfo,
  UserInfo,
} from "@openk9/http-api";
import { firstOrString } from "@openk9/search-ui-components";

export const isServer = typeof window === "undefined";

const persistVersion = "v1";

export type StateType = {
  sidebarOpen: boolean;
  toggleSidebar(): void;
  loginInfo: LoginInfo | null;
  userInfo: UserInfo | null;
  setLoginInfo(info: LoginInfo, userInfo: UserInfo): void;
  invalidateLogin(): void;
};

export const useStore = create<StateType>(
  persist(
    devtools((set, get) => ({
      sidebarOpen: true,
      toggleSidebar() {
        set((state) => ({ ...state, sidebarOpen: !state.sidebarOpen }));
      },

      loginInfo: null,
      userInfo: null,
      setLoginInfo(loginInfo: LoginInfo, userInfo: UserInfo) {
        set((state) => ({ ...state, loginInfo, userInfo }));
      },
      invalidateLogin() {
        set((state) => ({ ...state, loginInfo: null, userInfo: null }));
      },
    })),
    { name: "pq-admin-" + persistVersion },
  ),
);

export function useLoginInfo() {
  const loginInfo = useStore((s) => s.loginInfo);
  return loginInfo;
}

export function useLoginCheck({ isLoginPage } = { isLoginPage: false }) {
  const router = useRouter();

  const loginInfo = useStore((s) => s.loginInfo);
  const userInfo = useStore((s) => s.userInfo);
  const setLoginInfo = useStore((s) => s.setLoginInfo);
  const invalidateLogin = useStore((s) => s.invalidateLogin);

  const currentTimeSec = new Date().getTime() / 1000;
  const loginValid = loginInfo && userInfo && currentTimeSec < userInfo.exp;

  const redirect =
    (router.query.redirect && firstOrString(router.query.redirect)) || "/";

  //
  // Login page redirect logic
  //
  useEffect(() => {
    if (isLoginPage && loginValid) {
      // login page and login already done, redirect
      if (decodeURIComponent(redirect).startsWith("/login")) {
        router.push("/");
      } else {
        router.push(decodeURIComponent(redirect));
      }
    } else if (!loginValid) {
      // protected page ad no login, redirect to login
      router.push(`/login?redirect=${encodeURIComponent(router.pathname)}`);
    }
  }, [loginValid, isLoginPage]);

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

  return { loginInfo, userInfo, loginValid };
}
