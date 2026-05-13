import AddIcon from "@mui/icons-material/Add";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import KeyIcon from "@mui/icons-material/Key";
import RefreshIcon from "@mui/icons-material/Refresh";
import SearchIcon from "@mui/icons-material/Search";
import {
  Box,
  Button,
  Container,
  IconButton,
  InputAdornment,
  MenuItem,
  Paper,
  TextField,
  Toolbar,
  Typography,
} from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { GetApiKeysQuery, useGetApiKeysQuery, useTenantQuery } from "../../../graphql-generated";
import { ApiKeyCreatedModal } from "./ApiKeyCreatedModal";
import { ApiKeyDetailModal } from "./ApiKeyDetailModal";
import { ApiKeyTable } from "./ApiKeyTable";
import { CreateApiKeyModal } from "./CreateApiKeyModal";
import { RevokeApiKeyDialog } from "./RevokeApiKeyDialog";
import { deriveDisplayStatus, ApiKeyDisplayStatus } from "./labels";

type ApiKey = NonNullable<NonNullable<GetApiKeysQuery["apiKeys"]>[number]>;

const statusFilters: { value: "ALL" | ApiKeyDisplayStatus; label: string }[] = [
  { value: "ALL", label: "All statuses" },
  { value: "ACTIVE", label: "Active" },
  { value: "EXPIRED", label: "Expired" },
  { value: "REVOKED", label: "Revoked" },
];

export function ApiKeys() {
  const { tenantId = "" } = useParams();
  const navigate = useNavigate();

  const tenantQuery = useTenantQuery({
    variables: { id: tenantId },
    skip: !tenantId,
  });
  const tenantName = tenantQuery.data?.tenant?.tenantName ?? "";

  const apiKeysQuery = useGetApiKeysQuery({
    variables: { tenantId },
    skip: !tenantId,
    fetchPolicy: "cache-and-network",
  });

  const [search, setSearch] = React.useState("");
  const [statusFilter, setStatusFilter] = React.useState<"ALL" | ApiKeyDisplayStatus>("ALL");

  const [createOpen, setCreateOpen] = React.useState(false);
  const [createdKey, setCreatedKey] = React.useState<string | null>(null);
  const [detailRow, setDetailRow] = React.useState<ApiKey | null>(null);
  const [revokeRow, setRevokeRow] = React.useState<ApiKey | null>(null);

  const allRows = (apiKeysQuery.data?.apiKeys ?? [])
    .filter((r): r is ApiKey => !!r)
    .slice()
    .sort((a, b) => {
      const ta = a.createDate ? new Date(a.createDate).getTime() : 0;
      const tb = b.createDate ? new Date(b.createDate).getTime() : 0;
      return tb - ta;
    });

  const filteredRows = allRows.filter((row) => {
    const matchesSearch = !search || (row.name ?? "").toLowerCase().includes(search.toLowerCase());
    const status = deriveDisplayStatus(row.status, row.expirationDate);
    const matchesStatus = statusFilter === "ALL" || statusFilter === status;
    return matchesSearch && matchesStatus;
  });

  const isEmpty = !apiKeysQuery.loading && allRows.length === 0;

  return (
    <React.Fragment>
      <Toolbar>
        <IconButton edge="start" color="inherit" aria-label="back" onClick={() => navigate(`/tenants/${tenantId}`)} size="large">
          <ArrowBackIcon />
        </IconButton>
      </Toolbar>

      <Container maxWidth="lg">
        <Box sx={{ px: 4, py: 3 }}>
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              mb: 4,
              borderBottom: "2px solid",
              borderColor: "primary.main",
              pb: 2,
            }}
          >
            <Box sx={{ display: "flex", alignItems: "center" }}>
              <KeyIcon sx={{ fontSize: 40, mr: 2, color: "primary.main" }} />
              <Box>
                <Typography variant="h4" component="h1" color="primary">
                  API Keys
                </Typography>
                {tenantName && (
                  <Typography variant="body2" color="text.secondary">
                    Tenant: {tenantName}
                  </Typography>
                )}
              </Box>
            </Box>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreateOpen(true)} disabled={!tenantName}>
              Create API Key
            </Button>
          </Box>

          <Box sx={{ display: "flex", gap: 2, mb: 3, alignItems: "center" }}>
            <TextField
              size="small"
              placeholder="Search by name"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              sx={{ flexGrow: 1, maxWidth: 360 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon fontSize="small" />
                  </InputAdornment>
                ),
              }}
            />
            <TextField
              select
              size="small"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as typeof statusFilter)}
              sx={{ minWidth: 180 }}
            >
              {statusFilters.map((f) => (
                <MenuItem key={f.value} value={f.value}>
                  {f.label}
                </MenuItem>
              ))}
            </TextField>
            <IconButton onClick={() => apiKeysQuery.refetch()} aria-label="refresh">
              <RefreshIcon />
            </IconButton>
          </Box>

          <Paper variant="outlined">
            {isEmpty ? (
              <Box sx={{ p: 6, textAlign: "center" }}>
                <KeyIcon sx={{ fontSize: 56, color: "text.disabled", mb: 2 }} />
                <Typography variant="h6" gutterBottom>
                  No API keys yet
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                  Create your first API key to authenticate requests against this tenant.
                </Typography>
                <Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreateOpen(true)} disabled={!tenantName}>
                  Create API Key
                </Button>
              </Box>
            ) : (
              <ApiKeyTable
                loading={apiKeysQuery.loading && allRows.length === 0}
                rows={filteredRows}
                onView={(row) => setDetailRow(row)}
                onRevoke={(row) => setRevokeRow(row)}
              />
            )}
          </Paper>
        </Box>
      </Container>

      <CreateApiKeyModal
        tenantName={tenantName}
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={(_, apiKey) => {
          setCreateOpen(false);
          setCreatedKey(apiKey);
        }}
      />
      <ApiKeyCreatedModal open={!!createdKey} apiKey={createdKey} onClose={() => setCreatedKey(null)} />
      <ApiKeyDetailModal open={!!detailRow} apiKey={detailRow} onClose={() => setDetailRow(null)} />
      <RevokeApiKeyDialog
        open={!!revokeRow}
        apiKeyId={revokeRow?.id ?? null}
        apiKeyName={revokeRow?.name ?? null}
        onClose={() => setRevokeRow(null)}
        onRevoked={() => setRevokeRow(null)}
      />
    </React.Fragment>
  );
}
