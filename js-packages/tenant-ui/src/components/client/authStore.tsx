import React from "react";
import { Navigate, useLocation } from "react-router-dom";

const STORAGE_KEY = "tenantUiAuth";
const DEFAULT_EXPIRATION_MINUTES = 8 * 60;
const CHANGE_EVENT = "tenant-ui:auth-changed";

type StoredAuth = {
  token: string;
  type: "Basic";
  username: string;
  expiresAt: number;
};

type AuthContextValue = {
  isAuthenticated: boolean;
  username: string | null;
  signIn: (username: string, password: string, opts?: { expiresInMinutes?: number }) => void;
  signOut: () => void;
};

const AuthContext = React.createContext<AuthContextValue | null>(null);

function readStorage(): StoredAuth | null {
  const raw = sessionStorage.getItem(STORAGE_KEY);
  if (!raw) return null;
  try {
    const parsed = JSON.parse(raw) as StoredAuth;
    if (Date.now() >= parsed.expiresAt) {
      sessionStorage.removeItem(STORAGE_KEY);
      return null;
    }
    return parsed;
  } catch {
    sessionStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

function writeStorage(auth: StoredAuth): void {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
}

function clearStorage(): void {
  sessionStorage.removeItem(STORAGE_KEY);
}

export function getAuthHeader(): string | null {
  const auth = readStorage();
  return auth ? `${auth.type} ${auth.token}` : null;
}

export function forceSignOut(): void {
  clearStorage();
  window.dispatchEvent(new Event(CHANGE_EVENT));
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [auth, setAuth] = React.useState<StoredAuth | null>(() => readStorage());

  React.useEffect(() => {
    const onChange = () => setAuth(readStorage());
    window.addEventListener(CHANGE_EVENT, onChange);
    return () => window.removeEventListener(CHANGE_EVENT, onChange);
  }, []);

  React.useEffect(() => {
    if (!auth) return;
    const msUntilExpiry = auth.expiresAt - Date.now();
    if (msUntilExpiry <= 0) {
      clearStorage();
      setAuth(null);
      return;
    }
    const timer = setTimeout(() => {
      clearStorage();
      setAuth(null);
    }, msUntilExpiry);
    return () => clearTimeout(timer);
  }, [auth]);

  const signIn = React.useCallback<AuthContextValue["signIn"]>((username, password, opts) => {
    const expiresInMinutes = opts?.expiresInMinutes ?? DEFAULT_EXPIRATION_MINUTES;
    const next: StoredAuth = {
      token: btoa(`${username}:${password}`),
      type: "Basic",
      username,
      expiresAt: Date.now() + expiresInMinutes * 60_000,
    };
    writeStorage(next);
    setAuth(next);
  }, []);

  const signOut = React.useCallback<AuthContextValue["signOut"]>(() => {
    clearStorage();
    setAuth(null);
  }, []);

  const value = React.useMemo<AuthContextValue>(
    () => ({
      isAuthenticated: auth !== null,
      username: auth?.username ?? null,
      signIn,
      signOut,
    }),
    [auth, signIn, signOut]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = React.useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside <AuthProvider>");
  return ctx;
}

export function RequireAuth({ children }: { children: React.ReactElement }) {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  return children;
}

export function RedirectIfAuthenticated({
  children,
  to = "/",
}: {
  children: React.ReactElement;
  to?: string;
}) {
  const { isAuthenticated } = useAuth();
  if (isAuthenticated) {
    return <Navigate to={to} replace />;
  }
  return children;
}
