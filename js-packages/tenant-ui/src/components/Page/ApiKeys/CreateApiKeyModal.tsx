import { Box, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, TextField } from "@mui/material";
import React from "react";
import { ApiGroup, useCreateApiKeyMutation } from "../../../graphql-generated";
import { useToast } from "../../ToastProvider";
import { apiGroupDescription, apiGroupLabel } from "./labels";

// Ordine di presentazione nel menu; i valori vengono dall'enum generato.
const apiGroups = [ApiGroup.Administration, ApiGroup.Public, ApiGroup.Search, ApiGroup.Ingestion];

type Props = {
  tenantName: string;
  open: boolean;
  onClose: () => void;
  onCreated: (createdId: string, apiKey: string) => void;
};

export function CreateApiKeyModal({ tenantName, open, onClose, onCreated }: Props) {
  const showToast = useToast();
  const [name, setName] = React.useState("");
  const [apiGroup, setApiGroup] = React.useState<ApiGroup>(ApiGroup.Administration);
  const [expirationDate, setExpirationDate] = React.useState<string>("");

  const [createApiKey, { loading }] = useCreateApiKeyMutation({
    onCompleted(data) {
      if (data.createApiKey?.id && data.createApiKey.apiKey) {
        onCreated(data.createApiKey.id, data.createApiKey.apiKey);
        reset();
      }
    },
    onError(error) {
      showToast({ displayType: "error", title: "API key not created", content: error.message });
    },
    refetchQueries: ["GetApiKeys"],
  });

  function reset() {
    setName("");
    setApiGroup(ApiGroup.Administration);
    setExpirationDate("");
  }

  function handleClose() {
    reset();
    onClose();
  }

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    createApiKey({
      variables: {
        createApiKeyRequest: {
          tenantName,
          name: name.trim(),
          apiGroup,
          expirationDate: expirationDate ? new Date(expirationDate).toISOString() : null,
        },
      },
    });
  }

  const canSubmit = name.trim().length > 0 && !loading;

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <form onSubmit={handleSubmit}>
        <DialogTitle>Create API Key</DialogTitle>
        <DialogContent>
          <Box sx={{ display: "flex", flexDirection: "column", gap: 2, mt: 1 }}>
            <TextField
              label="API Key Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              fullWidth
              autoFocus
              helperText="A human-readable identifier for this key"
            />
            <TextField
              select
              label="API Group"
              value={apiGroup}
              onChange={(e) => setApiGroup(e.target.value as ApiGroup)}
              required
              fullWidth
              helperText={apiGroupDescription[apiGroup]}
            >
              {apiGroups.map((g) => (
                <MenuItem key={g} value={g}>
                  {apiGroupLabel[g]}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Expiration Date"
              type="datetime-local"
              value={expirationDate}
              onChange={(e) => setExpirationDate(e.target.value)}
              fullWidth
              InputLabelProps={{ shrink: true }}
              helperText="Leave empty for a non-expiring key"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose} disabled={loading}>
            Cancel
          </Button>
          <Button type="submit" variant="contained" disabled={!canSubmit} startIcon={loading ? <CircularProgress size={16} /> : null}>
            Create
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
