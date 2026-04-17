import React from "react";
import { AuthProvider, useAuth, hasAuthParams } from "react-oidc-context";
import { UserManager } from "oidc-client-ts";
import { Box, CircularProgress, Typography, Button, Paper } from "@mui/material";
import { BrandLogo } from "../BrandLogo";

/**
 * AuthenticationProvider wraps the app with react-oidc-context's AuthProvider.
 * It receives a pre-configured UserManager (created during bootstrap in index.tsx)
 * so OIDC settings are fetched once before the app mounts.
 */
export function AuthenticationProvider({
  userManager,
  children,
}: {
  userManager: UserManager;
  children: React.ReactNode;
}) {
  const onSigninCallback = () => {
    // Remove OIDC query params from URL after login redirect, then restore original path
    const returnUrl = sessionStorage.getItem("oidc_return_url") || "/tenant/";
    sessionStorage.removeItem("oidc_return_url");
    window.history.replaceState({}, document.title, returnUrl);
  };

  return (
    <AuthProvider userManager={userManager} onSigninCallback={onSigninCallback}>
      <AuthGate>{children}</AuthGate>
    </AuthProvider>
  );
}

/**
 * AuthGate handles the three auth states:
 * 1. Loading (initializing or processing redirect) → spinner
 * 2. Error → error UI with retry
 * 3. Not authenticated → auto-redirect to OIDC provider
 * 4. Authenticated → render children
 */
function AuthGate({ children }: { children: React.ReactNode }) {
  const auth = useAuth();
  const [hasTriedSignin, setHasTriedSignin] = React.useState(false);

  // Auto-redirect to OIDC provider if not authenticated and not already handling a callback
  React.useEffect(() => {
    if (!auth.isLoading && !auth.isAuthenticated && !hasAuthParams() && !hasTriedSignin) {
      // Save the current URL so we can restore it after login
      sessionStorage.setItem("oidc_return_url", window.location.pathname + window.location.search);
      setHasTriedSignin(true);
      auth.signinRedirect();
    }
  }, [auth.isLoading, auth.isAuthenticated, hasTriedSignin, auth]);

  if (auth.isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
        <CircularProgress />
      </Box>
    );
  }

  if (auth.error) {
    return <AuthErrorFallback error={auth.error} onRetry={() => auth.signinRedirect()} />;
  }

  if (!auth.isAuthenticated) {
    // Redirect in progress — show spinner
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
        <CircularProgress />
      </Box>
    );
  }

  return <>{children}</>;
}

function AuthErrorFallback({ error, onRetry }: { error: Error; onRetry: () => void }) {
  return (
    <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh" sx={{ backgroundColor: "background.default" }}>
      <Paper
        elevation={0}
        sx={{
          padding: 4,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          gap: 2,
          width: 400,
          border: 1,
          borderColor: "divider",
        }}
      >
        <Box display="flex" alignItems="center" gap={1} mb={1}>
          <BrandLogo width={40} height={40} colorFill="#c22525" />
          <Typography variant="h5" fontWeight={700} color="text.primary">
            OpenK9
          </Typography>
        </Box>
        <Typography variant="h6" color="error" align="center">
          Authentication Error
        </Typography>
        <Typography variant="body2" color="text.secondary" align="center">
          {error.message}
        </Typography>
        <Button variant="contained" color="primary" onClick={onRetry}>
          Retry Login
        </Button>
      </Paper>
    </Box>
  );
}

/**
 * Custom hook that provides a stable authentication interface to the rest of the app.
 * Components use this instead of react-oidc-context's useAuth directly,
 * keeping the migration surface small if the auth library ever changes.
 */
export function useAuthentication() {
  const auth = useAuth();
  return {
    isAuthenticated: auth.isAuthenticated,
    isInitializing: auth.isLoading,
    token: auth.user?.access_token,
    user: auth.user?.profile,
    login: () => auth.signinRedirect(),
    logout: () =>
      auth.signoutRedirect({ post_logout_redirect_uri: `${window.location.origin}/tenant/` }),
  };
}

/**
 * Returns the authenticated user's display name from OIDC claims.
 */
export async function getUserProfile() {
  // Access the OIDC user from the singleton UserManager (works outside React)
  const { getUserManager } = await import("./oidcClient");
  const manager = getUserManager();
  if (!manager) return { name: "User" };
  const user = await manager.getUser();
  return {
    name: user?.profile?.preferred_username || user?.profile?.name || "User",
    email: user?.profile?.email,
  };
}
