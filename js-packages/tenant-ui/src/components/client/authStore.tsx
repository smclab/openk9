import React from "react";
import { Navigate, useLocation } from "react-router-dom";

const STORAGE_KEY = "tenantUiAuth";
const DEFAULT_EXPIRATION_MINUTES = 8 * 60;
const CHANGE_EVENT = "tenant-ui:auth-changed";
const TOUCH_THROTTLE_MS = 60_000;

export const SESSION_WARNING_LEAD_MS = 5 * 60_000;

type StoredAuth = {
  token: string;
  type: "Basic";
  username: string;
  expiresAt: number;
};

type AuthContextValue = {
  isAuthenticated: boolean;
  username: string | null;
  expiresAt: number | null;
  signIn: (username: string, password: string, opts?: { expiresInMinutes?: number }) => void;
  signOut: () => void;
  extendSession: (opts?: { expiresInMinutes?: number }) => void;
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

let lastTouchAt = 0;

export function touchSession(opts?: { expiresInMinutes?: number }): void {
  const now = Date.now();
  if (now - lastTouchAt < TOUCH_THROTTLE_MS) return;
  const current = readStorage();
  if (!current) return;
  const expiresInMinutes = opts?.expiresInMinutes ?? DEFAULT_EXPIRATION_MINUTES;
  writeStorage({ ...current, expiresAt: now + expiresInMinutes * 60_000 });
  lastTouchAt = now;
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
    lastTouchAt = 0;
    clearStorage();
    setAuth(null);
  }, []);

  const extendSession = React.useCallback<AuthContextValue["extendSession"]>((opts) => {
    lastTouchAt = 0;
    touchSession(opts);
  }, []);

  const value = React.useMemo<AuthContextValue>(
    () => ({
      isAuthenticated: auth !== null,
      username: auth?.username ?? null,
      expiresAt: auth?.expiresAt ?? null,
      signIn,
      signOut,
      extendSession,
    }),
    [auth, signIn, signOut, extendSession]
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
