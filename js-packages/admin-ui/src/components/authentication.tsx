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
import { Box, Button, TextField, Typography, Paper } from "@mui/material";

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
  return true; // For basic auth, we "init" immediately and show the login form if not authenticated
};

export function AuthenticationProvider({ children }: { children: React.ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isInitializing, setIsInitializing] = useState<boolean>(true);

  // Basic auth state
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [tenantId, setTenantId] = useState("");

  useEffect(() => {
    if (isOauth2Enabled) {
      setIsAuthenticated(keycloak.authenticated ?? false);
      setIsInitializing(false);
    } else {
      const savedToken = sessionStorage.getItem("basic_auth_token");
      if (savedToken) {
        setIsAuthenticated(true);
      }
      setIsInitializing(false);
    }
  }, []);

  const loginBasic = (e: React.FormEvent) => {
    e.preventDefault();
    if (username && password && tenantId) {
      const basicToken = btoa(`${username}:${password}`);
      sessionStorage.setItem("basic_auth_token", basicToken);
      sessionStorage.setItem("basic_auth_tenant_id", tenantId);
      setIsAuthenticated(true);
    }
  };

  const logout = () => {
    if (isOauth2Enabled) {
      keycloak.logout();
    } else {
      sessionStorage.removeItem("basic_auth_token");
      sessionStorage.removeItem("basic_auth_tenant_id");
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
    } else {
      const basicToken = sessionStorage.getItem("basic_auth_token");
      const savedTenantId = sessionStorage.getItem("basic_auth_tenant_id");
      if (basicToken && savedTenantId) {
        return {
          Authorization: `Basic ${basicToken}`,
          "X-TENANT-ID": savedTenantId,
        };
      }
      return {};
    }
  };

  if (isInitializing) return null;

  if (!isOauth2Enabled && !isAuthenticated) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh" bgcolor="#f5f5f5">
        <Paper elevation={3} sx={{ padding: 4, display: "flex", flexDirection: "column", gap: 2, width: 350 }}>
          <Typography variant="h5" align="center" fontWeight="bold">
            Admin Login
          </Typography>
          <Typography variant="body2" align="center" color="textSecondary">
            Basic Authentication
          </Typography>
          <form onSubmit={loginBasic} style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
            <TextField
              label="Username"
              variant="outlined"
              size="small"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
            <TextField
              label="Password"
              type="password"
              variant="outlined"
              size="small"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <TextField
              label="Tenant ID"
              variant="outlined"
              size="small"
              value={tenantId}
              onChange={(e) => setTenantId(e.target.value)}
              required
            />
            <Button type="submit" variant="contained" color="primary">
              Login
            </Button>
          </form>
        </Paper>
      </Box>
    );
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
