import React, { createContext, useContext, useState, useEffect } from "react";
import { Box, Button, TextField, Typography, Paper } from "@mui/material";

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

  // Login form state
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

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

  const handleLoginSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (username && password) {
      const basicToken = btoa(`${username}:${password}`);
      login(basicToken);
    }
  };

  if (isInitializing) {
    return null; // or a loading spinner
  }

  if (!isAuthenticated) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh" bgcolor="#f5f5f5">
        <Paper elevation={3} sx={{ padding: 4, display: "flex", flexDirection: "column", gap: 2, width: 300 }}>
          <Typography variant="h5" align="center" fontWeight="bold">
            OpenK9 Tenant Login
          </Typography>
          <Typography variant="body2" align="center" color="textSecondary">
            Basic Authentication
          </Typography>
          <form onSubmit={handleLoginSubmit} style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
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
            <Button type="submit" variant="contained" color="primary">
              Login
            </Button>
          </form>
        </Paper>
      </Box>
    );
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
  return { username: "Admin" }; // Placeholder since we don't load user info from keycloak anymore
}
