import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField } from "@mui/material";
import { GetApiKeysQuery } from "../../../graphql-generated";
import { apiGroupLabel, deriveDisplayStatus } from "./labels";
import { StatusBadge } from "./statusBadge";

type ApiKey = NonNullable<NonNullable<GetApiKeysQuery["apiKeys"]>[number]>;

type Props = {
  open: boolean;
  apiKey: ApiKey | null;
  onClose: () => void;
};

function formatDate(value: string | null | undefined) {
  if (!value) return "—";
  return new Date(value).toLocaleString();
}

export function ApiKeyDetailModal({ open, apiKey, onClose }: Props) {
  if (!apiKey) return null;
  const status = deriveDisplayStatus(apiKey.status, apiKey.expirationDate);

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle>{apiKey.name ?? "API Key Details"}</DialogTitle>
      <DialogContent>
        <Box sx={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 2, mt: 1 }}>
          <TextField label="ID" value={apiKey.id ?? ""} InputProps={{ readOnly: true }} fullWidth />
          <TextField
            label="API Group"
            value={apiKey.apiGroup ? apiGroupLabel[apiKey.apiGroup] ?? apiKey.apiGroup : "—"}
            InputProps={{ readOnly: true }}
            fullWidth
          />
          <Box>
            <Box sx={{ fontSize: 12, color: "text.secondary", mb: 0.5 }}>Status</Box>
            <StatusBadge status={status} />
          </Box>
          <TextField label="Tenant ID" value={apiKey.tenantId ?? ""} InputProps={{ readOnly: true }} fullWidth />
          <TextField label="Prefix" value={apiKey.prefix ?? ""} InputProps={{ readOnly: true, sx: { fontFamily: "monospace" } }} fullWidth />
          <TextField label="Suffix" value={apiKey.suffix ?? ""} InputProps={{ readOnly: true, sx: { fontFamily: "monospace" } }} fullWidth />
          <TextField
            label="Hash"
            value={apiKey.hash ?? ""}
            InputProps={{ readOnly: true, sx: { fontFamily: "monospace" } }}
            fullWidth
            multiline
            sx={{ gridColumn: "1 / -1" }}
          />
          <TextField label="Created" value={formatDate(apiKey.createDate)} InputProps={{ readOnly: true }} fullWidth />
          <TextField label="Expires" value={formatDate(apiKey.expirationDate)} InputProps={{ readOnly: true }} fullWidth />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
}
