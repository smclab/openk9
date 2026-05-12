import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import WarningAmberIcon from "@mui/icons-material/WarningAmber";
import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, IconButton, TextField, Tooltip } from "@mui/material";
import React from "react";
import { useToast } from "../../ToastProvider";

type Props = {
  open: boolean;
  apiKey: string | null;
  onClose: () => void;
};

export function ApiKeyCreatedModal({ open, apiKey, onClose }: Props) {
  const showToast = useToast();
  const [copied, setCopied] = React.useState(false);

  async function handleCopy() {
    if (!apiKey) return;
    try {
      await navigator.clipboard.writeText(apiKey);
      setCopied(true);
      showToast({ displayType: "success", title: "Copied", content: "API key copied to clipboard" });
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
          <TextField value={apiKey ?? ""} fullWidth InputProps={{ readOnly: true, sx: { fontFamily: "monospace" } }} />
          <Tooltip title="Copy">
            <IconButton onClick={handleCopy} color="primary">
              <ContentCopyIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} variant="contained" disabled={!copied}>
          {copied ? "Done" : "Copy the key first"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
