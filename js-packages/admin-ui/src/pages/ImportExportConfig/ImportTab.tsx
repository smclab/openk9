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
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import UploadFileOutlinedIcon from "@mui/icons-material/UploadFileOutlined";
import UploadOutlinedIcon from "@mui/icons-material/UploadOutlined";
import {
  Alert,
  Box,
  Button,
  ButtonBase,
  Card,
  Chip,
  CircularProgress,
  FormControlLabel,
  MenuItem,
  Switch,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import { useMutation } from "@tanstack/react-query";
import type { ImportMode, ImportReport } from "openapi-generated";
import React from "react";
import { extractProblemDetails } from "utils/health";
import { useConfirmModal } from "utils/useConfirmModal";
import { ParsedPackage, readConfigPackage } from "./configPackage";
import { ImportReportPanel } from "./ImportReportPanel";
import { useTranslation } from "react-i18next";

const IMPORT_MODES: { value: ImportMode; labelKey: string }[] = [
  { value: "SKIP", labelKey: "mode-skip" },
  { value: "OVERWRITE", labelKey: "mode-overwrite" },
];

const REPORT_CONTENT_KEYS = [
  "report-content.actions",
  "report-content.conflicts",
  "report-content.secrets",
  "report-content.missing-references",
  "report-content.errors",
];

type SelectedFile = {
  name: string;
  parsed: ParsedPackage;
};

/** The whole request, so the confirmation gate and the call read one snapshot. */
type ImportInput = {
  selected: SelectedFile;
  mode: ImportMode;
  dryRun: boolean;
};

/** Uploads a configuration package, previews it and applies it. */
export function ImportTab() {
  const { t } = useTranslation();
  const restClient = useRestClient();
  const showToast = useToast();
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);

  const [file, setFile] = React.useState<SelectedFile | null>(null);
  const [fileError, setFileError] = React.useState<string | null>(null);
  const [mode, setMode] = React.useState<ImportMode>("SKIP");
  const [dryRun, setDryRun] = React.useState(true);
  const [report, setReport] = React.useState<ImportReport | null>(null);

  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.admin-settings.import-export.confirm-overwrite-title"),
    body: t("pages.admin-settings.import-export.confirm-overwrite-body"),
    labelConfirm: t("pages.admin-settings.import-export.confirm-overwrite-label"),
  });

  const importMutation = useMutation({
    mutationFn: ({ selected, mode: importMode, dryRun: preview }: ImportInput) =>
      restClient.configResource.importConfig(selected.parsed.value, importMode, preview),
    onSuccess: (outcome) => {
      // Only an applied import touches the tenant; a dry-run leaves every cache
      // valid. An import rewrites entities across the whole tenant, so every
      // cached list is stale — and Apollo reads cache-first, so nothing would
      // refresh on its own. Listing the queries is not an option here: the blast
      // radius is every entity type. A failing refetch does not change the
      // outcome, the caches are cleared either way.
      if (outcome.applied) {
        void apolloClient.resetStore().catch(() => undefined);
        void queryClient.invalidateQueries();
      }
      // No toast: the report below says the same thing in more detail, and being
      // an Alert it is announced on its own.
      setReport(outcome);
    },
    onError: (error: unknown) => {
      setReport(null);
      showToast({
        displayType: "error",
        title: t("pages.admin-settings.import-export.import-error-title"),
        // The backend answers with a Problem body, whose detail is the message
        // to show; the fallback covers a request that never reached it.
        content:
          extractProblemDetails(error, t).detail ??
          t("pages.admin-settings.import-export.import-error-content"),
      });
    },
  });

  const selectFile = async (selected: File | undefined) => {
    if (!selected) return;
    setReport(null);
    const result = await readConfigPackage(selected);
    if (result.ok) {
      setFile({ name: selected.name, parsed: result.parsed });
      setFileError(null);
    } else {
      setFile(null);
      setFileError(result.errorKey);
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
    const input: ImportInput = { selected: file, mode, dryRun };
    // A dry-run writes nothing, so it needs no confirmation even in OVERWRITE.
    if (input.mode === "OVERWRITE" && !input.dryRun && !(await openConfirmModal())) return;
    importMutation.mutate(input);
  };

  return (
    <Card sx={{ p: 2.5 }}>
      <Typography component="h2" variant="h3" fontWeight="600">
        Import configuration
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Upload a configuration package exported earlier.
      </Typography>

      <Box sx={{ display: "flex", flexDirection: { xs: "column", md: "row" }, gap: 2.5 }}>
        <Box sx={{ flex: 1, minWidth: 0 }}>
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
              minHeight: 140,
              p: 2.5,
              gap: 1.5,
              flexDirection: "column",
              borderRadius: 2.5,
              border: "1px dashed",
              borderColor: fileError ? "error.main" : "divider",
            }}
          >
            <UploadFileOutlinedIcon color="action" />
            <Box sx={{ textAlign: "center" }}>
              <Typography variant="body2">{file ? file.name : t("pages.admin-settings.import-export.dropzone-title")}</Typography>
              <Typography variant="body2" color="text.secondary">
                {file
                  ? t("pages.admin-settings.import-export.file-summary", {
                      schemaVersion: file.parsed.schemaVersion,
                      total: file.parsed.entities.length,
                    })
                  : "or click to select it"}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {t("pages.admin-settings.import-export.max-size")}
              </Typography>
            </Box>
          </ButtonBase>

          {fileError && (
            <Alert severity="error" sx={{ mt: 1.5 }}>
              {t(fileError)}
            </Alert>
          )}
        </Box>

        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
            <Typography variant="body2">{t("pages.admin-settings.import-export.mode-label")}</Typography>
            <Tooltip title={t("pages.admin-settings.import-export.mode-help")}>
              <InfoOutlinedIcon fontSize="small" color="action" />
            </Tooltip>
          </Box>
          <TextField
            select
            size="small"
            fullWidth
            value={mode}
            onChange={(event) => setMode(event.target.value === "OVERWRITE" ? "OVERWRITE" : "SKIP")}
            helperText={t("pages.admin-settings.import-export.mode-helper")}
            sx={{ mt: 0.5 }}
          >
            {IMPORT_MODES.map((importMode) => (
              <MenuItem key={importMode.value} value={importMode.value}>
                {t(`pages.admin-settings.import-export.${importMode.labelKey}`)}
              </MenuItem>
            ))}
          </TextField>

          <FormControlLabel
            sx={{ mt: 1 }}
            control={<Switch checked={dryRun} onChange={(event) => setDryRun(event.target.checked)} />}
            label={<Typography variant="body2">{t("pages.admin-settings.import-export.dry-run-label")}</Typography>}
          />
          <Typography variant="body2" color="text.secondary">
            {dryRun
              ? t("pages.admin-settings.import-export.dry-run-on")
              : t("pages.admin-settings.import-export.dry-run-off")}
          </Typography>
        </Box>

        <Box sx={{ flex: 1, minWidth: 0, p: 1.5, borderRadius: 2.5, border: "1px solid", borderColor: "divider" }}>
          <Typography variant="body2" fontWeight="600">
            {t("pages.admin-settings.import-export.report-content-title")}
          </Typography>
          <Box sx={{ mt: 0.5, display: "flex", flexDirection: "column", gap: 0.5 }}>
            {REPORT_CONTENT_KEYS.map((entry) => (
              <Box key={entry} sx={{ display: "flex", alignItems: "center", gap: 0.75 }}>
                <CheckCircleOutlineIcon fontSize="small" color="success" />
                <Typography variant="body2" color="text.secondary">
                  {t(`pages.admin-settings.import-export.${entry}`)}
                </Typography>
              </Box>
            ))}
          </Box>
        </Box>
      </Box>

      <Box sx={{ mt: 2.5, display: "flex", alignItems: "center", gap: 1.5, flexWrap: "wrap" }}>
        <Button
          variant="contained"
          startIcon={
            importMutation.isLoading ? (
              <CircularProgress size={16} color="inherit" aria-label={t("pages.admin-settings.import-export.import-in-progress-aria")} />
            ) : (
              <UploadOutlinedIcon />
            )
          }
          disabled={!file}
          aria-busy={importMutation.isLoading}
          onClick={() => void onImport()}
        >
          {t("pages.admin-settings.import-export.import-button")}
        </Button>
        {dryRun && <Chip size="small" color="success" variant="outlined" label="DRY RUN ON" />}
        <Typography variant="body2" color="text.secondary">
          {dryRun
            ? "A detailed report will be produced without applying any change."
            : t("pages.admin-settings.import-export.apply-warning")}
        </Typography>
      </Box>
      {/* Always rendered, so the change of content is announced. */}
      <Typography variant="body2" color="text.secondary" role="status" aria-live="polite" sx={{ minHeight: 20 }}>
        {importMutation.isLoading ? t("pages.admin-settings.import-export.importing-status") : ""}
      </Typography>

      {report && <ImportReportPanel report={report} />}

      <Alert severity="info" icon={<InfoOutlinedIcon />} sx={{ mt: 2.5 }}>
        <Typography variant="body2" fontWeight="600">
          Safe and transactional
        </Typography>
        <Typography variant="body2">
          Every import runs in a single transaction: at the first error the whole process is interrupted and no change
          is saved.
        </Typography>
      </Alert>

      <ConfirmModal />
    </Card>
  );
}
