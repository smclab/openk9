/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import { apolloClient } from "@components/apolloClient";
import { useToast } from "@components/Form/Form/ToastProvider";
import { queryClient, useRestClient } from "@components/queryClient";
import DownloadOutlinedIcon from "@mui/icons-material/DownloadOutlined";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import SwapVertOutlinedIcon from "@mui/icons-material/SwapVertOutlined";
import UploadFileOutlinedIcon from "@mui/icons-material/UploadFileOutlined";
import UploadOutlinedIcon from "@mui/icons-material/UploadOutlined";
import {
  Alert,
  AlertTitle,
  Box,
  Button,
  ButtonBase,
  CircularProgress,
  Divider,
  MenuItem,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import { useMutation } from "@tanstack/react-query";
import type { ImportMode, ImportResult } from "openapi-generated";
import React from "react";
import { useConfirmModal } from "utils/useConfirmModal";
import {
  downloadConfigPackage,
  ParsedPackage,
  parseConfigPackage,
  RedactedEntity,
  redactedEntities,
} from "./configPackage";
import { SettingsSection } from "./SettingsSection";

const IMPORT_MODES: { value: ImportMode; label: string }[] = [
  { value: "SKIP", label: "SKIP (default)" },
  { value: "OVERWRITE", label: "OVERWRITE" },
];

const MODE_HELP =
  "SKIP leaves the entities that already exist untouched. OVERWRITE replaces them with the ones in the package.";

const SECRETS_NOTE =
  'Sensitive values (secrets) are stripped and replaced with "__REDACTED__" before the file is exported.';

type SelectedFile = {
  name: string;
  parsed: ParsedPackage;
};

type ImportOutcome = {
  summary: ImportResult;
  secrets: RedactedEntity[];
};

/** The whole request, so the confirmation gate and the call read one snapshot. */
type ImportInput = {
  selected: SelectedFile;
  mode: ImportMode;
};

/** The message to show when a request fails; the backend answers 400 with an empty body. */
function requestErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof Error && error.message.length > 0) {
    return error.message;
  }
  return fallback;
}

/** Exports the tenant configuration to a file and imports one back. */
export function ImportExportSection() {
  const restClient = useRestClient();
  const showToast = useToast();
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);

  const [file, setFile] = React.useState<SelectedFile | null>(null);
  const [fileError, setFileError] = React.useState<string | null>(null);
  const [mode, setMode] = React.useState<ImportMode>("SKIP");
  const [outcome, setOutcome] = React.useState<ImportOutcome | null>(null);

  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Overwrite the current configuration?",
    body: "OVERWRITE replaces every entity of this tenant that also appears in the package. The operation cannot be undone.",
    labelConfirm: "Import and overwrite",
  });

  const exportMutation = useMutation({
    mutationFn: () => restClient.configResource.exportConfig(),
    onSuccess: (configPackage) => {
      downloadConfigPackage(configPackage);
      showToast({
        displayType: "success",
        title: "Configuration exported",
        content: `${configPackage.entities?.length ?? 0} entities downloaded.`,
      });
    },
    onError: (error: unknown) => {
      showToast({
        displayType: "error",
        title: "Export failed",
        content: requestErrorMessage(error, "The tenant configuration could not be exported."),
      });
    },
  });

  const importMutation = useMutation({
    mutationFn: ({ selected, mode: importMode }: ImportInput) =>
      restClient.configResource.importConfig(selected.parsed.value, importMode),
    onSuccess: (summary, { selected }) => {
      // An import rewrites entities across the whole tenant, so every cached
      // list is stale — and Apollo reads cache-first, so nothing would refresh
      // on its own. Listing the queries is not an option here: the blast radius
      // is every entity type. A failing refetch does not change the outcome,
      // the caches are cleared either way.
      void apolloClient.resetStore().catch(() => undefined);
      void queryClient.invalidateQueries();
      // No toast on success: the summary panel below reports the same counts,
      // and being an Alert it is announced on its own.
      setOutcome({ summary, secrets: redactedEntities(selected.parsed.entities) });
    },
    onError: (error: unknown) => {
      setOutcome(null);
      showToast({
        displayType: "error",
        title: "Import failed",
        content: requestErrorMessage(
          error,
          "The backend rejected the package. Nothing was applied: the import runs in a single transaction.",
        ),
      });
    },
  });

  const selectFile = async (selected: File | undefined) => {
    if (!selected) return;
    setOutcome(null);
    const result = parseConfigPackage(await selected.text());
    if (result.ok) {
      setFile({ name: selected.name, parsed: result.parsed });
      setFileError(null);
    } else {
      setFile(null);
      setFileError(result.error);
    }
  };

  const onDrop = (event: React.DragEvent<HTMLElement>) => {
    event.preventDefault();
    void selectFile(event.dataTransfer.files[0]);
  };

  const onImport = async () => {
    // Guarded here rather than by disabling the button: disabling it while the
    // request runs would drop the keyboard focus to the document body.
    if (!file || importMutation.isLoading) return;
    const input: ImportInput = { selected: file, mode };
    if (input.mode === "OVERWRITE" && !(await openConfirmModal())) return;
    importMutation.mutate(input);
  };

  return (
    <SettingsSection
      icon={<SwapVertOutlinedIcon />}
      title="Import / Export"
      description="Move the tenant configuration between environments."
    >
      <Box sx={{ display: "flex", flexDirection: { xs: "column", md: "row" }, gap: 3 }}>
        <Box sx={{ flex: 1 }}>
          <Typography variant="h4" fontWeight="600" gutterBottom>
            Export configuration
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Download the current tenant configuration as a versioned JSON file.
          </Typography>
          <Button
            variant="outlined"
            startIcon={
              exportMutation.isLoading ? <CircularProgress size={16} color="inherit" /> : <DownloadOutlinedIcon />
            }
            disabled={exportMutation.isLoading}
            onClick={() => exportMutation.mutate()}
          >
            Export configuration
          </Button>
        </Box>

        <Divider flexItem orientation="vertical" sx={{ display: { xs: "none", md: "block" } }} />

        <Box sx={{ flex: 1 }}>
          <Typography variant="h4" fontWeight="600" gutterBottom>
            Import configuration
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Select a JSON package exported from OpenK9.
          </Typography>

          <input
            ref={fileInputRef}
            type="file"
            accept="application/json,.json"
            hidden
            onChange={(event) => void selectFile(event.target.files?.[0])}
          />
          <ButtonBase
            onClick={() => fileInputRef.current?.click()}
            onDrop={onDrop}
            onDragOver={(event) => event.preventDefault()}
            sx={{
              width: "100%",
              p: 2.5,
              gap: 1.5,
              borderRadius: 2.5,
              border: "1px dashed",
              borderColor: fileError ? "error.main" : "divider",
              justifyContent: "center",
              textAlign: "left",
            }}
          >
            <UploadFileOutlinedIcon color="action" />
            <Box>
              <Typography variant="body2">{file ? file.name : "Drop the JSON file here"}</Typography>
              <Typography variant="body2" color="text.secondary">
                {file
                  ? `schema ${file.parsed.schemaVersion} · ${file.parsed.entities.length} entities`
                  : "or click to select it"}
              </Typography>
            </Box>
          </ButtonBase>

          {fileError && (
            <Alert severity="error" sx={{ mt: 1.5 }}>
              {fileError}
            </Alert>
          )}

          <Box sx={{ mt: 2, display: "flex", alignItems: "center", gap: 0.5 }}>
            <Typography variant="body2">Import mode</Typography>
            <Tooltip title={MODE_HELP}>
              <InfoOutlinedIcon fontSize="small" color="action" />
            </Tooltip>
          </Box>
          <TextField
            select
            size="small"
            value={mode}
            onChange={(event) => setMode(event.target.value === "OVERWRITE" ? "OVERWRITE" : "SKIP")}
            sx={{ mt: 0.5, minWidth: 200 }}
          >
            {IMPORT_MODES.map((importMode) => (
              <MenuItem key={importMode.value} value={importMode.value}>
                {importMode.label}
              </MenuItem>
            ))}
          </TextField>

          <Box sx={{ mt: 2 }}>
            <Button
              variant="contained"
              startIcon={
                importMutation.isLoading ? (
                  <CircularProgress size={16} color="inherit" aria-label="Import in progress" />
                ) : (
                  <UploadOutlinedIcon />
                )
              }
              disabled={!file}
              aria-busy={importMutation.isLoading}
              onClick={() => void onImport()}
            >
              Import configuration
            </Button>
            {/* Always rendered, so the change of content is announced. */}
            <Typography variant="body2" color="text.secondary" role="status" aria-live="polite" sx={{ minHeight: 20 }}>
              {importMutation.isLoading ? "Importing the configuration…" : ""}
            </Typography>
          </Box>
        </Box>
      </Box>

      {outcome && (
        <Alert severity="success" sx={{ mt: 2.5 }}>
          <AlertTitle>Import completed</AlertTitle>
          <Typography variant="body2">
            {outcome.summary.created ?? 0} created · {outcome.summary.overwritten ?? 0} overwritten ·{" "}
            {outcome.summary.skipped ?? 0} skipped
          </Typography>
          {outcome.secrets.length > 0 && (
            <Box sx={{ mt: 1 }}>
              <Typography variant="body2" fontWeight="600">
                Secrets to enter again:
              </Typography>
              <Box component="ul" sx={{ m: 0, pl: 2.5 }}>
                {outcome.secrets.map((secret) => (
                  <Typography component="li" variant="body2" key={`${secret.type}-${secret.key}`}>
                    {secret.type} “{secret.key}”: {secret.fields.join(", ")}
                  </Typography>
                ))}
              </Box>
            </Box>
          )}
        </Alert>
      )}

      <Box sx={{ mt: 2.5, display: "flex", alignItems: "center", gap: 0.5 }}>
        <InfoOutlinedIcon fontSize="small" color="action" />
        <Typography variant="body2" color="text.secondary">
          {SECRETS_NOTE}
        </Typography>
      </Box>

      <ConfirmModal />
    </SettingsSection>
  );
}
