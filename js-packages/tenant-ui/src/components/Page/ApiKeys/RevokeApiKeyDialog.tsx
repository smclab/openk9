import { Alert, Box, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, TextField, Typography } from "@mui/material";
import React from "react";
import { useRevokeApiKeyMutation } from "../../../graphql-generated";
import { useToast } from "../../ToastProvider";

type Props = {
  open: boolean;
  apiKeyId: string | null;
  apiKeyName: string | null;
  onClose: () => void;
  onRevoked: () => void;
};

export function RevokeApiKeyDialog({ open, apiKeyId, apiKeyName, onClose, onRevoked }: Props) {
  const showToast = useToast();
  const [typed, setTyped] = React.useState("");

  const [revoke, { loading }] = useRevokeApiKeyMutation({
    onCompleted() {
      showToast({ displayType: "success", title: "API key revoked", content: "" });
      onRevoked();
      reset();
    },
    onError(error) {
      showToast({ displayType: "error", title: "Revoke failed", content: error.message });
    },
    refetchQueries: ["GetApiKeys"],
  });

  function reset() {
    setTyped("");
  }

  function handleClose() {
    reset();
    onClose();
  }

  function handleConfirm() {
    if (!apiKeyId) return;
    revoke({ variables: { id: apiKeyId } });
  }

  const canConfirm = !!apiKeyName && typed === apiKeyName && !loading;

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Revoke API Key</DialogTitle>
      <DialogContent>
        <Alert severity="error" sx={{ mb: 2 }}>
          This action cannot be undone. The key will be invalidated immediately.
        </Alert>
        <Typography variant="body2" sx={{ mb: 2 }}>
          To confirm, type the key name <strong>{apiKeyName}</strong> below:
        </Typography>
        <Box>
          <TextField
            value={typed}
            onChange={(e) => setTyped(e.target.value)}
            placeholder={apiKeyName ?? ""}
            fullWidth
            autoFocus
            disabled={loading}
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} disabled={loading}>
          Cancel
        </Button>
        <Button color="error" variant="contained" disabled={!canConfirm} onClick={handleConfirm} startIcon={loading ? <CircularProgress size={16} /> : null}>
          Revoke
        </Button>
      </DialogActions>
    </Dialog>
  );
}
