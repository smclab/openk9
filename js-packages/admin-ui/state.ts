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
import { LoginInfo, UserInfo } from "@openk9/rest-api";
import { firstOrString } from "./components/utils";

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
    devtools((set) => ({
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

export function useLoginRedirect() {
  const router = useRouter();
  const loginInfo = useStore((s) => s.loginInfo);
  const route = router.route;
  const redirect = firstOrString(router.query.redirect ?? "");
  useEffect(() => {
    const isLoginPage = route === "/login";
    if (isLoginPage && loginInfo) {
      // login page and login already done, redirect
      router.push(decodeURIComponent(redirect || "/"));
    } else if (!loginInfo && !isLoginPage) {
      // protected page ad no login, redirect to login
      router.push(
        `/login?redirect=${encodeURIComponent(window.location.href)}`,
      );
    }
  }, [loginInfo, redirect, route]); // do not include router here, it causes an infinite loop
}
