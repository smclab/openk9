import React from "react";
import { Navigate, useLocation } from "react-router-dom";

// Opaque storage key avoids advertising what the entry holds. The value is
// XOR-obfuscated against a per-tab nonce; this slows down accidental dumps
// and casual inspection but does NOT defend against XSS — primary mitigation
// is the strict CSP in public/index.html.
const STORAGE_KEY = "tu_ctx";
const NONCE_KEY = "tu_n";
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

function getNonce(): string {
  let nonce = sessionStorage.getItem(NONCE_KEY);
  if (!nonce) {
    const bytes = new Uint8Array(16);
    crypto.getRandomValues(bytes);
    nonce = Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
    sessionStorage.setItem(NONCE_KEY, nonce);
  }
  return nonce;
}

function bytesToBase64(bytes: Uint8Array): string {
  let bin = "";
  for (let i = 0; i < bytes.length; i++) bin += String.fromCharCode(bytes[i]);
  return window.btoa(bin);
}

function base64ToBytes(value: string): Uint8Array {
  const bin = window.atob(value);
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
  return out;
}

function obfuscate(value: string): string {
  const nonce = getNonce();
  const valueBytes = new TextEncoder().encode(value);
  const xored = new Uint8Array(valueBytes.length);
  for (let i = 0; i < valueBytes.length; i++) {
    xored[i] = valueBytes[i] ^ nonce.charCodeAt(i % nonce.length);
  }
  return bytesToBase64(xored);
}

function deobfuscate(value: string): string {
  const nonce = getNonce();
  const xored = base64ToBytes(value);
  const out = new Uint8Array(xored.length);
  for (let i = 0; i < xored.length; i++) {
    out[i] = xored[i] ^ nonce.charCodeAt(i % nonce.length);
  }
  return new TextDecoder().decode(out);
}

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
    const parsed = JSON.parse(deobfuscate(raw)) as StoredAuth;
    if (Date.now() >= parsed.expiresAt) {
      clearStorage();
      return null;
    }
    return parsed;
  } catch {
    clearStorage();
    return null;
  }
}

function writeStorage(auth: StoredAuth): void {
  sessionStorage.setItem(STORAGE_KEY, obfuscate(JSON.stringify(auth)));
}

function clearStorage(): void {
  sessionStorage.removeItem(STORAGE_KEY);
  sessionStorage.removeItem(NONCE_KEY);
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
      token: window.btoa(`${username}:${password}`),
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
