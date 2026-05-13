import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import VisibilityIcon from "@mui/icons-material/Visibility";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import WarningAmberIcon from "@mui/icons-material/WarningAmber";
import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, IconButton, TextField, Tooltip, Typography } from "@mui/material";
import React from "react";
import { useToast } from "../../ToastProvider";

type Props = {
  open: boolean;
  apiKey: string | null;
  onClose: () => void;
};

const CLIPBOARD_CLEAR_MS = 60_000;

export function ApiKeyCreatedModal({ open, apiKey, onClose }: Props) {
  const showToast = useToast();
  const [copied, setCopied] = React.useState(false);
  const [revealed, setRevealed] = React.useState(false);
  const clearTimerRef = React.useRef<number | null>(null);
  const [clearAtMs, setClearAtMs] = React.useState<number | null>(null);
  const [secondsLeft, setSecondsLeft] = React.useState<number | null>(null);

  React.useEffect(() => {
    if (!open) {
      setCopied(false);
      setRevealed(false);
      setClearAtMs(null);
      setSecondsLeft(null);
      if (clearTimerRef.current !== null) {
        window.clearTimeout(clearTimerRef.current);
        clearTimerRef.current = null;
      }
    }
  }, [open]);

  React.useEffect(() => {
    if (clearAtMs === null) return;
    const tick = () => {
      const remaining = Math.max(0, Math.ceil((clearAtMs - Date.now()) / 1000));
      setSecondsLeft(remaining);
    };
    tick();
    const id = window.setInterval(tick, 1000);
    return () => window.clearInterval(id);
  }, [clearAtMs]);

  async function handleCopy() {
    if (!apiKey) return;
    try {
      await navigator.clipboard.writeText(apiKey);
      setCopied(true);
      showToast({ displayType: "success", title: "Copied", content: "API key in clipboard. Auto-cleared in 60s." });

      if (clearTimerRef.current !== null) window.clearTimeout(clearTimerRef.current);
      setClearAtMs(Date.now() + CLIPBOARD_CLEAR_MS);
      clearTimerRef.current = window.setTimeout(async () => {
        try {
          const current = await navigator.clipboard.readText();
          if (current === apiKey) {
            await navigator.clipboard.writeText("");
          }
        } catch {
          // clipboard read can fail without focus or permission; best effort
        }
        setClearAtMs(null);
        setSecondsLeft(null);
        clearTimerRef.current = null;
      }, CLIPBOARD_CLEAR_MS);
    } catch {
      showToast({ displayType: "error", title: "Copy failed", content: "Select and copy manually" });
    }
  }

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm" disableEscapeKeyDown>
      <DialogTitle>API Key Created</DialogTitle>
      <DialogContent>
        <Alert severity="warning" icon={<WarningAmberIcon />} sx={{ mb: 2 }}>
          This is the only time the API key will be shown. Copy and store it now in a safe place.
        </Alert>
        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
          <TextField
            value={apiKey ?? ""}
            type={revealed ? "text" : "password"}
            fullWidth
            InputProps={{
              readOnly: true,
              sx: { fontFamily: "monospace" },
              endAdornment: (
                <Tooltip title={revealed ? "Hide" : "Reveal"}>
                  <IconButton size="small" onClick={() => setRevealed((r) => !r)} edge="end">
                    {revealed ? <VisibilityOffIcon fontSize="small" /> : <VisibilityIcon fontSize="small" />}
                  </IconButton>
                </Tooltip>
              ),
            }}
          />
          <Tooltip title="Copy">
            <IconButton onClick={handleCopy} color="primary">
              <ContentCopyIcon />
            </IconButton>
          </Tooltip>
        </Box>
        {secondsLeft !== null && (
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: "block" }}>
            Clipboard will be cleared in {secondsLeft}s
          </Typography>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} variant="contained" disabled={!copied}>
          {copied ? "Done" : "Copy the key first"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
