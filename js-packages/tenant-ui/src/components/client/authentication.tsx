import React, { createContext, useContext, useState, useEffect } from "react";
import { BasicLoginForm } from "./BasicLoginForm";

type AuthenticationContextValue = {
  isAuthenticated: boolean;
  token?: string;
  login: (token: string) => void;
  logout: () => void;
};

const AuthenticationContext = createContext<AuthenticationContextValue>(null as any);

export function AuthenticationProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | undefined>(undefined);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isInitializing, setIsInitializing] = useState<boolean>(true);

  useEffect(() => {
    const savedToken = sessionStorage.getItem("basic_auth_token");
    if (savedToken) {
      setToken(savedToken);
      setIsAuthenticated(true);
    }
    setIsInitializing(false);
  }, []);

  const login = (newToken: string) => {
    sessionStorage.setItem("basic_auth_token", newToken);
    setToken(newToken);
    setIsAuthenticated(true);
  };

  const logout = () => {
    sessionStorage.removeItem("basic_auth_token");
    setToken(undefined);
    setIsAuthenticated(false);
  };

  if (isInitializing) return null;

  if (!isAuthenticated) {
    return <BasicLoginForm title="OpenK9 Tenant Login" onLogin={login} />;
  }

  return (
    <AuthenticationContext.Provider value={{ isAuthenticated, token, login, logout }}>
      {children}
    </AuthenticationContext.Provider>
  );
}

export function useAuthentication() {
  return useContext(AuthenticationContext);
}

export async function getUserProfile() {
  return { username: "Admin" };
}
