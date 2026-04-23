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
import React, { useEffect, useState, createContext, useContext } from "react";
import { UserManager, WebStorageStateStore, type User } from "oidc-client-ts";
import { BasicLoginForm } from "./BasicLoginForm";

declare global {
  interface Window {
    KEYCLOAK_URL: string;
    KEYCLOAK_REALM: string;
    KEYCLOAK_CLIENT_ID: string;
  }
}

const oauth2BaseUrl = typeof window !== "undefined" ? window.KEYCLOAK_URL : "";

export const isOauth2Enabled = !!oauth2BaseUrl && oauth2BaseUrl !== "DISABLED" && oauth2BaseUrl !== "";

const buildAuthority = (): string => {
  const realm = window.KEYCLOAK_REALM;
  const base = oauth2BaseUrl.replace(/\/+$/, "");
  return realm ? `${base}/realms/${realm}` : base;
};

const redirectUri = typeof window !== "undefined" ? `${window.location.origin}/admin/` : "";

export const userManager: UserManager | null = isOauth2Enabled
  ? new UserManager({
      authority: buildAuthority(),
      client_id: window.KEYCLOAK_CLIENT_ID,
      redirect_uri: redirectUri,
      post_logout_redirect_uri: redirectUri,
      response_type: "code",
      scope: "openid",
      automaticSilentRenew: true,
      loadUserInfo: false,
      monitorSession: false,
      userStore: new WebStorageStateStore({ store: window.sessionStorage }),
      stateStore: new WebStorageStateStore({ store: window.sessionStorage }),
    })
  : null;

if (isOauth2Enabled) {
  console.log("[auth] OIDC enabled", {
    authority: buildAuthority(),
    client_id: window.KEYCLOAK_CLIENT_ID,
    redirect_uri: redirectUri,
  });
}

const isRedirectCallback = (): boolean => {
  const params = new URLSearchParams(window.location.search);
  return params.has("code") && params.has("state");
};

export async function authInit(): Promise<boolean> {
  if (!userManager) return true;
  try {
    if (isRedirectCallback()) {
      console.log("[auth] processing redirect callback");
      const callbackUser = await userManager.signinRedirectCallback();
      console.log("[auth] callback user:", {
        has_access_token: !!callbackUser?.access_token,
        expired: callbackUser?.expired,
        expires_at: callbackUser?.expires_at,
        token_type: callbackUser?.token_type,
      });
      window.history.replaceState({}, document.title, window.location.pathname);
    }
    const user = await userManager.getUser();
    console.log("[auth] authInit user after init:", {
      present: !!user,
      expired: user?.expired,
      has_token: !!user?.access_token,
    });
    if (!user || user.expired) {
      console.log("[auth] no valid user, redirecting to IdP");
      await userManager.signinRedirect();
      await new Promise<void>(() => {});
      return false;
    }
    return true;
  } catch (err) {
    console.error("[auth] authInit failed, forcing redirect", err);
    await userManager.removeUser();
    await userManager.signinRedirect();
    await new Promise<void>(() => {});
    return false;
  }
}

export async function getAccessToken(): Promise<string | null> {
  if (!userManager) return null;
  const user = await userManager.getUser();
  if (!user || user.expired) return null;
  return user.access_token ?? null;
}

export async function getAuthHeaders(): Promise<Record<string, string>> {
  if (isOauth2Enabled) {
    const token = await getAccessToken();
    if (!token) {
      console.warn("[auth] getAuthHeaders: no access token available");
      return {};
    }
    return { Authorization: `Bearer ${token}` };
  }
  const basicToken = sessionStorage.getItem("basic_auth_token");
  return basicToken ? { Authorization: `Basic ${basicToken}` } : {};
}

export async function getUserProfile() {
  if (isOauth2Enabled && userManager) {
    const user = await userManager.getUser();
    return user?.profile ?? {};
  }
  return { username: "Admin" };
}

type AuthenticationContextValue = {
  isAuthenticated: boolean;
  isOauth2: boolean;
  logout: () => void;
  getAuthHeaders: () => Promise<Record<string, string>>;
};

const AuthenticationContext = createContext<AuthenticationContextValue>(null as any);

function OidcAuthenticationProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    if (!userManager) return;
    let cancelled = false;

    userManager.getUser().then((u) => {
      if (!cancelled) setUser(u);
    });

    const onUserLoaded = (u: User) => {
      if (!cancelled) setUser(u);
    };
    const onUserUnloaded = () => {
      if (!cancelled) setUser(null);
    };
    const onSilentRenewError = (err: Error) => {
      console.error("OIDC silent renew error", err);
    };

    userManager.events.addUserLoaded(onUserLoaded);
    userManager.events.addUserUnloaded(onUserUnloaded);
    userManager.events.addSilentRenewError(onSilentRenewError);

    return () => {
      cancelled = true;
      userManager!.events.removeUserLoaded(onUserLoaded);
      userManager!.events.removeUserUnloaded(onUserUnloaded);
      userManager!.events.removeSilentRenewError(onSilentRenewError);
    };
  }, []);

  const logout = () => {
    void userManager?.signoutRedirect();
  };

  const value: AuthenticationContextValue = {
    isAuthenticated: !!user && !user.expired,
    isOauth2: true,
    logout,
    getAuthHeaders,
  };

  return <AuthenticationContext.Provider value={value}>{children}</AuthenticationContext.Provider>;
}

function BasicAuthenticationProvider({ children }: { children: React.ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(
    () => !!sessionStorage.getItem("basic_auth_token"),
  );

  const handleBasicLogin = (token: string) => {
    sessionStorage.setItem("basic_auth_token", token);
    setIsAuthenticated(true);
  };

  const logout = () => {
    sessionStorage.removeItem("basic_auth_token");
    setIsAuthenticated(false);
  };

  if (!isAuthenticated) {
    return <BasicLoginForm title="Admin Login" onLogin={handleBasicLogin} />;
  }

  const value: AuthenticationContextValue = {
    isAuthenticated,
    isOauth2: false,
    logout,
    getAuthHeaders,
  };

  return <AuthenticationContext.Provider value={value}>{children}</AuthenticationContext.Provider>;
}

export function AuthenticationProvider({ children }: { children: React.ReactNode }) {
  if (!isOauth2Enabled || !userManager) {
    return <BasicAuthenticationProvider>{children}</BasicAuthenticationProvider>;
  }
  return <OidcAuthenticationProvider>{children}</OidcAuthenticationProvider>;
}

export function useAuthentication() {
  return useContext(AuthenticationContext);
}
