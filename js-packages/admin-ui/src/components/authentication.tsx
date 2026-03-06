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
import React, { useState, useEffect, createContext, useContext } from "react";
import Keycloak from "keycloak-js";
import { BasicLoginForm } from "./BasicLoginForm";

export const isOauth2Enabled = !!window.KEYCLOAK_URL && window.KEYCLOAK_URL !== "DISABLED" && window.KEYCLOAK_URL !== "";

export const keycloak = isOauth2Enabled
  ? new Keycloak({
      url: window.KEYCLOAK_URL,
      realm: window.KEYCLOAK_REALM,
      clientId: window.KEYCLOAK_CLIENT_ID,
    })
  : ({} as any);

type AuthenticationContextValue = {
  isAuthenticated: boolean;
  isOauth2: boolean;
  logout: () => void;
  getAuthHeaders: () => Promise<Record<string, string>>;
};

const AuthenticationContext = createContext<AuthenticationContextValue>(null as any);

export const authInit = async () => {
  if (isOauth2Enabled) {
    return await keycloak.init({ onLoad: "login-required" });
  }
  return true;
};

export function AuthenticationProvider({ children }: { children: React.ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isInitializing, setIsInitializing] = useState<boolean>(true);

  useEffect(() => {
    if (isOauth2Enabled) {
      setIsAuthenticated(keycloak.authenticated ?? false);
    } else {
      const savedToken = sessionStorage.getItem("basic_auth_token");
      if (savedToken) {
        setIsAuthenticated(true);
      }
    }
    setIsInitializing(false);
  }, []);

  const handleBasicLogin = (token: string) => {
    sessionStorage.setItem("basic_auth_token", token);
    setIsAuthenticated(true);
  };

  const logout = () => {
    if (isOauth2Enabled) {
      keycloak.logout();
    } else {
      sessionStorage.removeItem("basic_auth_token");
      setIsAuthenticated(false);
    }
  };

  const getAuthHeaders = async (): Promise<Record<string, string>> => {
    if (isOauth2Enabled) {
      if (keycloak.authenticated) {
        await keycloak.updateToken(30);
        return { Authorization: `Bearer ${keycloak.token}` };
      }
      return {};
    }
    const basicToken = sessionStorage.getItem("basic_auth_token");
    if (basicToken) {
      return { Authorization: `Basic ${basicToken}` };
    }
    return {};
  };

  if (isInitializing) return null;

  if (!isOauth2Enabled && !isAuthenticated) {
    return <BasicLoginForm title="Admin Login" onLogin={handleBasicLogin} />;
  }

  return (
    <AuthenticationContext.Provider value={{ isAuthenticated, isOauth2: isOauth2Enabled, logout, getAuthHeaders }}>
      {children}
    </AuthenticationContext.Provider>
  );
}

export async function getUserProfile() {
  if (isOauth2Enabled) {
    return await keycloak.loadUserInfo();
  }
  return { username: "Admin" };
}

export function useAuthentication() {
  return useContext(AuthenticationContext);
}
