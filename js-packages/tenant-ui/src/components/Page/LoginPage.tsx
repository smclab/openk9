import React from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Alert, AlertTitle, Box, Button, CircularProgress, IconButton, InputAdornment, Paper, TextField, Typography } from "@mui/material";
import { ErrorOutline, LockOutlined, PersonOutline, Visibility, VisibilityOff } from "@mui/icons-material";
import { BrandLogo } from "../BrandLogo";
import { useAuth } from "../client/authStore";

type LoginErrorKind = "invalid-credentials" | "unauthorized" | "server" | "network";

type LoginError = {
  kind: LoginErrorKind;
  title: string;
  detail?: string;
};

const MAX_BACKOFF_MS = 30_000;
const BACKOFF_BASE_MS = 1_000;

function computeBackoff(failedAttempts: number): number {
  if (failedAttempts < 3) return 0;
  return Math.min(MAX_BACKOFF_MS, BACKOFF_BASE_MS * 2 ** (failedAttempts - 3));
}

async function validateCredentials(username: string, password: string): Promise<Response> {
  const token = btoa(`${username}:${password}`);
  return fetch("/api/tenant-manager/graphql", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Basic ${token}`,
    },
    body: JSON.stringify({ query: "{ __typename }" }),
  });
}

export function LoginPage() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation() as { state?: { from?: { pathname?: string } } };
  const redirectTo = location.state?.from?.pathname || "/";

  const [username, setUsername] = React.useState("admin");
  const [password, setPassword] = React.useState("");
  const [showPassword, setShowPassword] = React.useState(false);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<LoginError | null>(null);
  const [errorShakeKey, setErrorShakeKey] = React.useState(0);
  const [failedAttempts, setFailedAttempts] = React.useState(0);
  const [cooldownMs, setCooldownMs] = React.useState(0);

  React.useEffect(() => {
    if (cooldownMs <= 0) return;
    const started = Date.now();
    const initial = cooldownMs;
    const interval = setInterval(() => {
      const elapsed = Date.now() - started;
      const remaining = Math.max(0, initial - elapsed);
      setCooldownMs(remaining);
      if (remaining <= 0) clearInterval(interval);
    }, 250);
    return () => clearInterval(interval);
  }, [cooldownMs]);

  const reportError = (err: LoginError) => {
    setError(err);
    setErrorShakeKey((k) => k + 1);
    const nextAttempts = failedAttempts + 1;
    setFailedAttempts(nextAttempts);
    const backoff = computeBackoff(nextAttempts);
    if (backoff > 0) setCooldownMs(backoff);
  };

  const clearError = () => {
    if (error) setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password || loading || cooldownMs > 0) return;
    setLoading(true);
    setError(null);
    try {
      const response = await validateCredentials(username, password);
      if (response.status === 401) {
        reportError({
          kind: "invalid-credentials",
          title: "Credenziali non valide",
          detail: "Username o password errati. Riprova.",
        });
        return;
      }
      if (response.status === 403) {
        reportError({
          kind: "unauthorized",
          title: "Utente non autorizzato",
          detail: "Le credenziali sono corrette ma l'utente non ha i permessi per accedere.",
        });
        return;
      }
      if (!response.ok) {
        reportError({
          kind: "server",
          title: "Errore del server",
          detail: `Il server ha risposto con stato ${response.status}. Riprova più tardi.`,
        });
        return;
      }
      setFailedAttempts(0);
      signIn(username, password);
      navigate(redirectTo, { replace: true });
    } catch {
      reportError({
        kind: "network",
        title: "Errore di rete",
        detail: "Impossibile contattare il server. Verifica la connessione e riprova.",
      });
    } finally {
      setLoading(false);
    }
  };

  const fieldError = error?.kind === "invalid-credentials";
  const submitDisabled = loading || !username || !password || cooldownMs > 0;
  const cooldownSeconds = Math.ceil(cooldownMs / 1000);

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
          borderRadius: 2,
        }}
      >
        <Box display="flex" alignItems="center" gap={1} mb={1}>
          <BrandLogo width={40} height={40} colorFill="#c22525" />
          <Typography variant="h5" fontWeight={700} color="text.primary">
            Open
          </Typography>
          <Typography variant="h4" fontWeight={700} color="text.primary">
            K9
          </Typography>
        </Box>
        <Typography variant="h6" align="center" fontWeight="bold" color="text.primary">
          Sign in to Tenant Admin
        </Typography>

        {error && (
          <Alert
            key={errorShakeKey}
            severity="error"
            variant="filled"
            icon={<ErrorOutline fontSize="inherit" />}
            sx={{
              width: "100%",
              alignItems: "flex-start",
              borderRadius: 1.5,
              animation: "tenantUiShake 0.45s cubic-bezier(.36,.07,.19,.97) both",
              "@keyframes tenantUiShake": {
                "10%, 90%": { transform: "translate3d(-1px, 0, 0)" },
                "20%, 80%": { transform: "translate3d(2px, 0, 0)" },
                "30%, 50%, 70%": { transform: "translate3d(-4px, 0, 0)" },
                "40%, 60%": { transform: "translate3d(4px, 0, 0)" },
              },
            }}
          >
            <AlertTitle sx={{ mb: error.detail ? 0.5 : 0, fontWeight: 700 }}>{error.title}</AlertTitle>
            {error.detail}
          </Alert>
        )}

        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 16, width: "100%" }}>
          <TextField
            label="Username"
            variant="outlined"
            size="small"
            fullWidth
            value={username}
            onChange={(e) => {
              setUsername(e.target.value);
              clearError();
            }}
            required
            disabled={loading}
            error={fieldError}
            autoComplete="username"
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <PersonOutline fontSize="small" />
                </InputAdornment>
              ),
            }}
          />
          <TextField
            label="Password"
            type={showPassword ? "text" : "password"}
            variant="outlined"
            size="small"
            fullWidth
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              clearError();
            }}
            required
            disabled={loading}
            error={fieldError}
            autoComplete="current-password"
            autoFocus
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <LockOutlined fontSize="small" />
                </InputAdornment>
              ),
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton
                    aria-label={showPassword ? "Nascondi password" : "Mostra password"}
                    onClick={() => setShowPassword((s) => !s)}
                    edge="end"
                    size="small"
                    tabIndex={-1}
                  >
                    {showPassword ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />
          <Button
            type="submit"
            variant="contained"
            color="primary"
            size="large"
            disabled={submitDisabled}
            startIcon={loading ? <CircularProgress size={20} color="inherit" /> : undefined}
          >
            {loading ? "Autenticazione…" : cooldownMs > 0 ? `Riprova tra ${cooldownSeconds}s` : "Login"}
          </Button>
        </form>
      </Paper>
    </Box>
  );
}
