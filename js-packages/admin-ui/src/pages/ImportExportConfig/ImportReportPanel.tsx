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
import { Alert, AlertTitle, Box, Chip, Divider, Typography } from "@mui/material";
import type { Action, ImportReport, PlannedAction } from "openapi-generated";
import React from "react";
import { useTranslation } from "react-i18next";

/** The planned actions grouped by kind, in the order they are reported. */
const ACTION_ORDER: Action[] = ["CREATE", "OVERWRITE", "SKIP"];

/** The counts, worded as a plan or as an outcome depending on what ran. */
function counts({ created = 0, overwritten = 0, skipped = 0, applied }: ImportReport): string[] {
  return applied
    ? [`${created} created`, `${overwritten} overwritten`, `${skipped} skipped`]
    : [`${created} to create`, `${overwritten} to overwrite`, `${skipped} to skip`];
}

/**
 * The severity of the whole report: a blocking error is a failure, a dry-run is
 * an announcement of what would happen, an applied import is a success.
 */
function severity({ blockingErrors, applied }: ImportReport): "error" | "info" | "success" {
  if ((blockingErrors ?? []).length > 0) return "error";
  return applied ? "success" : "info";
}

function titleKey({ blockingErrors, applied, dryRun }: ImportReport): string {
  if ((blockingErrors ?? []).length > 0) return "report-title.blocked";
  if (applied) return "report-title.applied";
  return dryRun ? "report-title.preview" : "report-title.not-applied";
}

function Section({ heading, children }: { heading: string; children: React.ReactNode }) {
  return (
    <Box sx={{ mt: 1.5 }}>
      <Typography variant="body2" fontWeight="600">
        {heading}
      </Typography>
      <Box component="ul" sx={{ m: 0, pl: 2.5 }}>
        {children}
      </Box>
    </Box>
  );
}

function actionsOfKind(actions: PlannedAction[], kind: Action): PlannedAction[] {
  return actions.filter((action) => action.action === kind);
}

/**
 * Renders an `ImportReport`, from either a dry-run or an applied import: the two
 * carry the same fields, so they are shown the same way and only the wording of
 * the outcome differs.
 */
export function ImportReportPanel({ report }: { report: ImportReport }) {
  const { t } = useTranslation();
  const actions = report.actions ?? [];
  const missingReferences = report.missingReferences ?? [];
  const secretsToReenter = report.secretsToReenter ?? [];
  const blockingErrors = report.blockingErrors ?? [];

  return (
    <Alert severity={severity(report)} sx={{ mt: 2.5 }}>
      <AlertTitle>{t(`pages.admin-settings.import-export.${titleKey(report)}`)}</AlertTitle>

      <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
        {counts(report).map((count) => (
          <Chip size="small" key={count} label={count} />
        ))}
        <Chip size="small" variant="outlined" label={`mode ${report.mode ?? "SKIP"}`} />
      </Box>

      {blockingErrors.length > 0 && (
        <Section heading={t("pages.admin-settings.import-export.report-content.errors")}>
          {blockingErrors.map((error) => (
            <Typography component="li" variant="body2" key={error}>
              {error}
            </Typography>
          ))}
        </Section>
      )}

      {secretsToReenter.length > 0 && (
        <>
          <Divider sx={{ mt: 1.5 }} />
          <Section heading={t("pages.admin-settings.import-export.report-secrets-heading")}>
            {secretsToReenter.map((secret, index) => (
              <Typography component="li" variant="body2" key={`${secret.ref}-${index}`}>
                {secret.type} “{secret.key}”: {(secret.fields ?? []).join(", ")}
              </Typography>
            ))}
          </Section>
        </>
      )}

      {missingReferences.length > 0 && (
        <Section heading={t("pages.admin-settings.import-export.report-missing-heading")}>
          {missingReferences.map((reference) => (
            <Typography
              component="li"
              variant="body2"
              key={`${reference.fromRef}-${reference.relationship}-${reference.targetHandle}`}
            >
              {reference.fromKey} · {reference.relationship} → {reference.targetHandle}
            </Typography>
          ))}
        </Section>
      )}

      {ACTION_ORDER.map((kind) => {
        const ofKind = actionsOfKind(actions, kind);
        if (ofKind.length === 0) return null;
        return (
          <Section key={kind} heading={`${kind} (${ofKind.length})`}>
            {ofKind.map((action, index) => (
              <Typography component="li" variant="body2" key={`${action.ref}-${index}`}>
                {action.type} · {action.ref}
                {action.existingId !== undefined && ` (existing id ${action.existingId})`}
              </Typography>
            ))}
          </Section>
        );
      })}
    </Alert>
  );
}
