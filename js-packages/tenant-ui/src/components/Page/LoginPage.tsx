import React from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Alert, Box, Button, CircularProgress, Paper, TextField, Typography } from "@mui/material";
import { BrandLogo } from "../BrandLogo";
import { useAuth } from "../client/authStore";

type LoginError = { title: string; detail?: string };

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

  const [username, setUsername] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<LoginError | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password || loading) return;
    setLoading(true);
    setError(null);
    try {
      const response = await validateCredentials(username, password);
      if (response.status === 401) {
        setError({ title: "Credenziali non valide", detail: "Username o password errati." });
        return;
      }
      if (response.status === 403) {
        setError({ title: "Utente non autorizzato", detail: "L'utente non ha i permessi necessari." });
        return;
      }
      if (!response.ok) {
        setError({ title: "Errore del server", detail: `Il server ha risposto con stato ${response.status}.` });
        return;
      }
      signIn(username, password);
      navigate(redirectTo, { replace: true });
    } catch {
      setError({ title: "Errore di rete", detail: "Impossibile contattare il server. Verifica la connessione." });
    } finally {
      setLoading(false);
    }
  };

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
          width: 380,
          border: 1,
          borderColor: "divider",
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
          <Alert severity="error" sx={{ width: "100%" }}>
            <strong>{error.title}</strong>
            {error.detail && (
              <>
                <br />
                {error.detail}
              </>
            )}
          </Alert>
        )}

        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 16, width: "100%" }}>
          <TextField
            label="Username"
            variant="outlined"
            size="small"
            fullWidth
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            disabled={loading}
            autoComplete="username"
            autoFocus
          />
          <TextField
            label="Password"
            type="password"
            variant="outlined"
            size="small"
            fullWidth
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={loading}
            autoComplete="current-password"
          />
          <Button
            type="submit"
            variant="contained"
            color="primary"
            size="large"
            disabled={loading || !username || !password}
            startIcon={loading ? <CircularProgress size={20} color="inherit" /> : undefined}
          >
            {loading ? "Autenticazione…" : "Login"}
          </Button>
        </form>
      </Paper>
    </Box>
  );
}
