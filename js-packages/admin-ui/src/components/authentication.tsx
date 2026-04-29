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

const redirectUri = typeof window !== "undefined" ? `${window.location.origin}/admin/` : "";

let userManager: UserManager | null = null;
let oauthEnabled = false;

export function getUserManager(): UserManager | null {
  return userManager;
}

export function isOauth2Enabled(): boolean {
  return oauthEnabled;
}

async function loadOauthConfig(): Promise<OauthConfig | null> {
  try {
    const res = await fetch(OAUTH2_SETTINGS_ENDPOINT, { credentials: "same-origin" });
    if (!res.ok) {
      console.log("[auth] oauth2/settings not ok", res.status);
      return null;
    }
    const data = (await res.json()) as OauthSettingsResponse;
    if (!data.issuerUri || data.issuerUri === "DISABLED" || !data.clientId) {
      console.log("[auth] oauth2 disabled or incomplete", data);
      return null;
    }
    return { issuerUri: data.issuerUri, clientId: data.clientId };
  } catch (err) {
    console.warn("[auth] oauth2/settings fetch failed, falling back to Basic", err);
    return null;
  }
}

function buildUserManager(config: OauthConfig): UserManager {
  return new UserManager({
    authority: config.issuerUri,
    client_id: config.clientId,
    redirect_uri: redirectUri,
    post_logout_redirect_uri: redirectUri,
    response_type: "code",
    scope: "openid",
    automaticSilentRenew: true,
    loadUserInfo: false,
    monitorSession: false,
    userStore: new WebStorageStateStore({ store: window.sessionStorage }),
    stateStore: new WebStorageStateStore({ store: window.sessionStorage }),
  });
}

const isRedirectCallback = (): boolean => {
  const params = new URLSearchParams(window.location.search);
  return params.has("code") && params.has("state");
};

export async function authInit(): Promise<boolean> {
  const config = await loadOauthConfig();
  if (!config) {
    oauthEnabled = false;
    userManager = null;
    return true;
  }

  oauthEnabled = true;
  userManager = buildUserManager(config);
  console.log("[auth] OIDC enabled", { authority: config.issuerUri, client_id: config.clientId, redirect_uri: redirectUri });

  try {
    if (isRedirectCallback()) {
      console.log("[auth] processing redirect callback");
      const callbackUser = await userManager.signinRedirectCallback();
      console.log("[auth] callback user:", {
        has_access_token: !!callbackUser?.access_token,
        expired: callbackUser?.expired,
      });
      window.history.replaceState({}, document.title, window.location.pathname);
    }
    const user = await userManager.getUser();
    console.log("[auth] authInit user:", {
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
  if (oauthEnabled) {
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
  if (oauthEnabled && userManager) {
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
    const um = userManager;

    um.getUser().then((u) => {
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

    um.events.addUserLoaded(onUserLoaded);
    um.events.addUserUnloaded(onUserUnloaded);
    um.events.addSilentRenewError(onSilentRenewError);

    return () => {
      cancelled = true;
      um.events.removeUserLoaded(onUserLoaded);
      um.events.removeUserUnloaded(onUserUnloaded);
      um.events.removeSilentRenewError(onSilentRenewError);
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
  if (!oauthEnabled || !userManager) {
    return <BasicAuthenticationProvider>{children}</BasicAuthenticationProvider>;
  }
  return <OidcAuthenticationProvider>{children}</OidcAuthenticationProvider>;
}

export function useAuthentication() {
  return useContext(AuthenticationContext);
}
