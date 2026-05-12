import { Box, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, TextField } from "@mui/material";
import React from "react";
import { useCreateApiKeyMutation } from "../../../graphql-generated";
import { useToast } from "../../ToastProvider";
import { apiGroupDescription, apiGroupLabel } from "./labels";

const apiGroups = ["ADMINISTRATION", "PUBLIC", "SEARCH", "INGESTION"] as const;

type Props = {
  tenantName: string;
  open: boolean;
  onClose: () => void;
  onCreated: (createdId: string, apiKey: string) => void;
};

export function CreateApiKeyModal({ tenantName, open, onClose, onCreated }: Props) {
  const showToast = useToast();
  const [name, setName] = React.useState("");
  const [apiGroup, setApiGroup] = React.useState<(typeof apiGroups)[number]>("ADMINISTRATION");
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
    setApiGroup("ADMINISTRATION");
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
          apiGroup: apiGroup as any,
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
              onChange={(e) => setApiGroup(e.target.value as (typeof apiGroups)[number])}
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
