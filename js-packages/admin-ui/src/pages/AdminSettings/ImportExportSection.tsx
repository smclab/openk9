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
import type { TFunction } from "i18next";
import type { ImportMode, ImportReport } from "openapi-generated";
import React from "react";
import { useTranslation } from "react-i18next";
import { useConfirmModal } from "utils/useConfirmModal";
import {
  downloadConfigPackage,
  ParsedPackage,
  parseConfigPackage,
  RedactedEntity,
  redactedEntities,
} from "./configPackage";
import { SettingsSection } from "./SettingsSection";

// Both labels carry the backend enum token, so both keep it verbatim; they go
// through the catalog anyway, so a locale can gloss either one.
const getImportModes = (t: TFunction): { value: ImportMode; label: string }[] => [
  { value: "SKIP", label: t("pages.admin-settings.import-export.mode-skip") },
  { value: "OVERWRITE", label: t("pages.admin-settings.import-export.mode-overwrite") },
];

type SelectedFile = {
  name: string;
  parsed: ParsedPackage;
};

type ImportOutcome = {
  summary: ImportReport;
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
  const { t } = useTranslation();
  const restClient = useRestClient();
  const showToast = useToast();
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);

  const [file, setFile] = React.useState<SelectedFile | null>(null);
  const [fileErrorKey, setFileErrorKey] = React.useState<string | null>(null);
  const [mode, setMode] = React.useState<ImportMode>("SKIP");
  const [outcome, setOutcome] = React.useState<ImportOutcome | null>(null);

  const importModes = React.useMemo(() => getImportModes(t), [t]);

  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.admin-settings.import-export.confirm-overwrite-title"),
    body: t("pages.admin-settings.import-export.confirm-overwrite-body"),
    labelConfirm: t("pages.admin-settings.import-export.confirm-overwrite-label"),
  });

  const exportMutation = useMutation({
    mutationFn: () => restClient.configResource.exportConfig(),
    onSuccess: (configPackage) => {
      downloadConfigPackage(configPackage);
      showToast({
        displayType: "success",
        title: t("pages.admin-settings.import-export.export-success-title"),
        content: t("pages.admin-settings.import-export.export-success-content", {
          total: configPackage.entities?.length ?? 0,
        }),
      });
    },
    onError: (error: unknown) => {
      showToast({
        displayType: "error",
        title: t("pages.admin-settings.import-export.export-error-title"),
        content: requestErrorMessage(error, t("pages.admin-settings.import-export.export-error-content")),
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
        title: t("pages.admin-settings.import-export.import-error-title"),
        content: requestErrorMessage(error, t("pages.admin-settings.import-export.import-error-content")),
      });
    },
  });

  const selectFile = async (selected: File | undefined) => {
    if (!selected) return;
    setOutcome(null);
    const result = parseConfigPackage(await selected.text());
    if (result.ok) {
      setFile({ name: selected.name, parsed: result.parsed });
      setFileErrorKey(null);
    } else {
      setFile(null);
      setFileErrorKey(result.errorKey);
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
      title={t("pages.admin-settings.import-export.title")}
      description={t("pages.admin-settings.import-export.description")}
    >
      <Box sx={{ display: "flex", flexDirection: { xs: "column", md: "row" }, gap: 3 }}>
        <Box sx={{ flex: 1 }}>
          <Typography variant="h4" fontWeight="600" gutterBottom>
            {t("pages.admin-settings.import-export.export-title")}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {t("pages.admin-settings.import-export.export-description")}
          </Typography>
          <Button
            variant="outlined"
            startIcon={
              exportMutation.isLoading ? <CircularProgress size={16} color="inherit" /> : <DownloadOutlinedIcon />
            }
            disabled={exportMutation.isLoading}
            onClick={() => exportMutation.mutate()}
          >
            {t("pages.admin-settings.import-export.export-title")}
          </Button>
        </Box>

        <Divider flexItem orientation="vertical" sx={{ display: { xs: "none", md: "block" } }} />

        <Box sx={{ flex: 1 }}>
          <Typography variant="h4" fontWeight="600" gutterBottom>
            {t("pages.admin-settings.import-export.import-title")}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {t("pages.admin-settings.import-export.import-description")}
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
              borderColor: fileErrorKey ? "error.main" : "divider",
              justifyContent: "center",
              textAlign: "left",
            }}
          >
            <UploadFileOutlinedIcon color="action" />
            <Box>
              <Typography variant="body2">
                {file ? file.name : t("pages.admin-settings.import-export.dropzone-title")}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {file
                  ? t("pages.admin-settings.import-export.file-summary", {
                      schemaVersion: file.parsed.schemaVersion,
                      total: file.parsed.entities.length,
                    })
                  : t("pages.admin-settings.import-export.dropzone-hint")}
              </Typography>
            </Box>
          </ButtonBase>

          {fileErrorKey && (
            <Alert severity="error" sx={{ mt: 1.5 }}>
              {t(fileErrorKey)}
            </Alert>
          )}

          <Box sx={{ mt: 2, display: "flex", alignItems: "center", gap: 0.5 }}>
            <Typography variant="body2">{t("pages.admin-settings.import-export.mode-label")}</Typography>
            <Tooltip title={t("pages.admin-settings.import-export.mode-help")}>
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
            {importModes.map((importMode) => (
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
                  <CircularProgress
                    size={16}
                    color="inherit"
                    aria-label={t("pages.admin-settings.import-export.import-in-progress-aria")}
                  />
                ) : (
                  <UploadOutlinedIcon />
                )
              }
              disabled={!file}
              aria-busy={importMutation.isLoading}
              onClick={() => void onImport()}
            >
              {t("pages.admin-settings.import-export.import-title")}
            </Button>
            {/* Always rendered, so the change of content is announced. */}
            <Typography variant="body2" color="text.secondary" role="status" aria-live="polite" sx={{ minHeight: 20 }}>
              {importMutation.isLoading ? t("pages.admin-settings.import-export.importing-status") : ""}
            </Typography>
          </Box>
        </Box>
      </Box>

      {outcome && (
        <Alert severity="success" sx={{ mt: 2.5 }}>
          <AlertTitle>{t("pages.admin-settings.import-export.result-title")}</AlertTitle>
          <Typography variant="body2">
            {t("pages.admin-settings.import-export.result-counts", {
              created: outcome.summary.created ?? 0,
              overwritten: outcome.summary.overwritten ?? 0,
              skipped: outcome.summary.skipped ?? 0,
            })}
          </Typography>
          {outcome.secrets.length > 0 && (
            <Box sx={{ mt: 1 }}>
              <Typography variant="body2" fontWeight="600">
                {t("pages.admin-settings.import-export.secrets-to-reenter")}
              </Typography>
              <Box component="ul" sx={{ m: 0, pl: 2.5 }}>
                {outcome.secrets.map((secret) => (
                  <Typography component="li" variant="body2" key={`${secret.type}-${secret.key}`}>
                    {t("pages.admin-settings.import-export.secret-entry", {
                      type: secret.type,
                      entityKey: secret.key,
                      fields: secret.fields.join(", "),
                    })}
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
          {t("pages.admin-settings.import-export.secrets-note")}
        </Typography>
      </Box>

      <ConfirmModal />
    </SettingsSection>
  );
}
