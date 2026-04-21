import React from "react";
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from "@mui/material";
import { AccessTime } from "@mui/icons-material";
import { SESSION_WARNING_LEAD_MS, useAuth } from "./authStore";

function formatRemaining(ms: number): string {
  const totalSec = Math.max(0, Math.ceil(ms / 1000));
  const m = Math.floor(totalSec / 60);
  const s = totalSec % 60;
  return `${m}m ${s.toString().padStart(2, "0")}s`;
}

export function SessionExpirationWarning() {
  const { expiresAt, extendSession, signOut } = useAuth();
  const [open, setOpen] = React.useState(false);
  const [remainingMs, setRemainingMs] = React.useState(0);

  React.useEffect(() => {
    if (!expiresAt) {
      setOpen(false);
      return;
    }
    const warningAt = expiresAt - SESSION_WARNING_LEAD_MS;
    const msUntilWarning = warningAt - Date.now();
    if (msUntilWarning <= 0) {
      setOpen(true);
      return;
    }
    setOpen(false);
    const timer = setTimeout(() => setOpen(true), msUntilWarning);
    return () => clearTimeout(timer);
  }, [expiresAt]);

  React.useEffect(() => {
    if (!open || !expiresAt) return;
    const tick = () => setRemainingMs(Math.max(0, expiresAt - Date.now()));
    tick();
    const interval = setInterval(tick, 1000);
    return () => clearInterval(interval);
  }, [open, expiresAt]);

  if (!open) return null;

  return (
    <Dialog
      open
      onClose={() => {}}
      disableEscapeKeyDown
      PaperProps={{ sx: { borderRadius: 2, maxWidth: 420 } }}
    >
      <DialogTitle sx={{ display: "flex", alignItems: "center", gap: 1 }}>
        <AccessTime color="warning" />
        Sessione in scadenza
      </DialogTitle>
      <DialogContent>
        <DialogContentText>
          La tua sessione scadrà tra <strong>{formatRemaining(remainingMs)}</strong>. Vuoi estenderla?
        </DialogContentText>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={() => signOut()} color="inherit">
          Logout
        </Button>
        <Button
          onClick={() => {
            extendSession();
            setOpen(false);
          }}
          variant="contained"
          color="primary"
        >
          Estendi sessione
        </Button>
      </DialogActions>
    </Dialog>
  );
}
