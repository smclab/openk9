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
import { useToast } from "@components/Form/Form/ToastProvider";
import { useRestClient } from "@components/queryClient";
import AccountTreeOutlinedIcon from "@mui/icons-material/AccountTreeOutlined";
import CropFreeOutlinedIcon from "@mui/icons-material/CropFreeOutlined";
import DownloadOutlinedIcon from "@mui/icons-material/DownloadOutlined";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import {
  Box,
  Button,
  Card,
  Checkbox,
  CircularProgress,
  Divider,
  FormControlLabel,
  Radio,
  RadioGroup,
  Typography,
} from "@mui/material";
import { useMutation, useQuery } from "@tanstack/react-query";
import type { ConfigEntityType } from "openapi-generated";
import React from "react";
import { extractProblemDetails } from "utils/health";
import { downloadConfigPackage, redactedEntities } from "./configPackage";
import { selectableTypes } from "./exportTypes";
import { useTranslation } from "react-i18next";

/** How many more types each "Load more" reveals. */
const PAGE_SIZE = 8;

type Depth = "deep" | "shallow";

const DEPTHS: { value: Depth; labelKey: string; descriptionKey: string; Icon: typeof AccountTreeOutlinedIcon }[] = [
  {
    value: "deep",
    labelKey: "depth-deep-label",
    descriptionKey: "depth-deep-description",
    Icon: AccountTreeOutlinedIcon,
  },
  {
    value: "shallow",
    labelKey: "depth-shallow-label",
    descriptionKey: "depth-shallow-description",
    Icon: CropFreeOutlinedIcon,
  },
];

/** Exports the tenant configuration: which types to take, and how deep. */
export function ExportTab() {
  const { t } = useTranslation();
  const restClient = useRestClient();
  const showToast = useToast();

  const [selected, setSelected] = React.useState<ReadonlySet<ConfigEntityType>>(new Set());
  const [depth, setDepth] = React.useState<Depth>("deep");

  const [visibleCount, setVisibleCount] = React.useState(PAGE_SIZE);

  const toggleType = (type: ConfigEntityType) =>
    setSelected((current) => {
      const next = new Set(current);
      if (!next.delete(type)) next.add(type);
      return next;
    });

  // The registry does not depend on the tenant and does not change at runtime,
  // so it is read once. A failure is not fatal: the export falls back to
  // sending every type of the selected groups, which is what it did before.
  const entityTypesQuery = useQuery({
    queryKey: ["config-entity-types"],
    queryFn: () => restClient.configResource.entityTypes(),
    staleTime: Infinity,
  });

  // the tab offers what the backend says it can export, nothing more
  const types = React.useMemo(
    () => (entityTypesQuery.data ? selectableTypes(entityTypesQuery.data) : []),
    [entityTypesQuery.data],
  );

  // slice() takes care of a count larger than the list, so nothing has to be
  // reset when the registry answers and the list grows from empty
  const shownTypes = types.slice(0, visibleCount);

  const exportMutation = useMutation({
    mutationFn: () => restClient.configResource.exportConfig([...selected], depth === "deep"),
    onSuccess: (configPackage) => {
      downloadConfigPackage(configPackage);
      const entities = configPackage.entities ?? [];
      const redacted = redactedEntities(entities).reduce((total, entity) => total + entity.fields.length, 0);
      showToast({
        displayType: "success",
        title: t("pages.admin-settings.import-export.export-success-title"),
        content: t("pages.admin-settings.import-export.export-success-detail", { total: entities.length, redacted }),
      });
    },
    onError: (error: unknown) => {
      showToast({
        displayType: "error",
        title: t("pages.admin-settings.import-export.export-error-title"),
        // The backend answers with a Problem body, whose detail is the message
        // to show; the fallback covers a request that never reached it.
        content: extractProblemDetails(error, t).detail ?? t("pages.admin-settings.import-export.export-error-content"),
      });
    },
  });

  return (
    <Card sx={{ p: 2.5 }}>
      <Box sx={{ display: "flex", flexDirection: { xs: "column", md: "row" }, gap: 2.5 }}>
        <Box sx={{ flex: 2, minWidth: 0 }}>
          <Typography component="h2" variant="h3" fontWeight="600">
            Export configuration
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Select the configuration types to include in the package.
          </Typography>

          <Typography variant="body2" fontWeight="600" sx={{ mb: 1 }}>
            {t("pages.admin-settings.import-export.types-title")}
          </Typography>
          <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", sm: "repeat(3, 1fr)" }, gap: 1 }}>
            {shownTypes.map((type) => (
              <FormControlLabel
                key={type}
                control={<Checkbox checked={selected.has(type)} onChange={() => toggleType(type)} />}
                label={
                  <Typography variant="body2" noWrap>
                    {type}
                  </Typography>
                }
                sx={{ m: 0, px: 1, borderRadius: 2, border: "1px solid", borderColor: "divider", minWidth: 0 }}
              />
            ))}
            {shownTypes.length < types.length && (
              <Button
                onClick={() => setVisibleCount((current) => current + PAGE_SIZE)}
                sx={{
                  px: 1,
                  borderRadius: 2,
                  border: "1px solid",
                  borderColor: "primary.main",
                  color: "primary.main",
                  textTransform: "uppercase",
                  minWidth: 0,
                }}
              >
                <Typography variant="body2" color="primary.main" noWrap>
                  {t("pages.admin-settings.import-export.load-more")}
                </Typography>
              </Button>
            )}
          </Box>

          <Box sx={{ mt: 2, display: "flex", gap: 1 }}>
            <Button variant="outlined" size="small" onClick={() => setSelected(new Set(types))}>
              {t("pages.admin-settings.import-export.select-all")}
            </Button>
            <Button variant="outlined" size="small" onClick={() => setSelected(new Set())}>
              {t("pages.admin-settings.import-export.deselect-all")}
            </Button>
          </Box>

          <Box sx={{ mt: 2.5, display: "flex", alignItems: "center", gap: 1.5, flexWrap: "wrap" }}>
            <Button
              variant="contained"
              startIcon={
                exportMutation.isLoading ? <CircularProgress size={16} color="inherit" /> : <DownloadOutlinedIcon />
              }
              disabled={exportMutation.isLoading}
              onClick={() => exportMutation.mutate()}
            >
              {t("pages.admin-settings.import-export.export-button")}
            </Button>
            <Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
              <InfoOutlinedIcon fontSize="small" color="action" />
              <Typography variant="body2" color="text.secondary">
                {selected.size === 0
                  ? t("pages.admin-settings.import-export.export-hint-all")
                  : t("pages.admin-settings.import-export.export-hint-selection")}
              </Typography>
            </Box>
          </Box>
        </Box>

        <Divider flexItem orientation="vertical" sx={{ display: { xs: "none", md: "block" } }} />

        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Typography component="h2" variant="h3" fontWeight="600">
            {t("pages.admin-settings.import-export.depth-title")}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
            {t("pages.admin-settings.import-export.depth-description")}
          </Typography>

          <RadioGroup
            value={depth}
            onChange={(event) => setDepth(event.target.value === "shallow" ? "shallow" : "deep")}
          >
            {DEPTHS.map(({ value, labelKey, descriptionKey, Icon }) => (
              <FormControlLabel
                key={value}
                value={value}
                control={<Radio />}
                sx={{
                  m: 0,
                  mb: 1,
                  p: 1.5,
                  alignItems: "flex-start",
                  borderRadius: 2.5,
                  border: "1px solid",
                  borderColor: depth === value ? "primary.main" : "divider",
                }}
                label={
                  <Box sx={{ display: "flex", gap: 1.5 }}>
                    <Icon fontSize="small" color={depth === value ? "primary" : "action"} sx={{ mt: 0.25 }} />
                    <Box>
                      <Typography variant="body2" fontWeight="600">
                        {t(`pages.admin-settings.import-export.${labelKey}`)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {t(`pages.admin-settings.import-export.${descriptionKey}`)}
                      </Typography>
                    </Box>
                  </Box>
                }
              />
            ))}
          </RadioGroup>

          <Box sx={{ mt: 1, p: 1.5, borderRadius: 2.5, border: "1px solid", borderColor: "divider" }}>
            <Box sx={{ display: "flex", alignItems: "center", gap: 0.75 }}>
              <LockOutlinedIcon fontSize="small" color="action" />
              <Typography variant="body2" fontWeight="600">
                {t("pages.admin-settings.import-export.secrets-title")}
              </Typography>
            </Box>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              {t("pages.admin-settings.import-export.secrets-note")}
            </Typography>
          </Box>
        </Box>
      </Box>
    </Card>
  );
}
