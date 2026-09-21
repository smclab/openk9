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
import {
  AlertColor,
  Box,
  Chip,
  Link as MuiLink,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@mui/material";
import React from "react";
import { Link as RouterLink } from "react-router-dom";
import { Status } from "../../graphql-generated";

// the status is the generated enum, so the maps below stop compiling the day a
// status is added and a rename cannot silently slip past the comparisons
export type AlignmentOutcome = {
  dataIndexId?: string | number | null;
  indexName?: string | null;
  status?: Status | null;
  reason?: string | null;
};

const STATUS_COLOR: Record<Status, AlertColor> = {
  [Status.Applied]: "success",
  [Status.TemplateOnly]: "info",
  [Status.Skipped]: "warning",
  [Status.CloseRequired]: "warning",
  [Status.Failed]: "error",
};

const STATUS_LABEL: Record<Status, string> = {
  [Status.Applied]: "Applied",
  [Status.TemplateOnly]: "Template only",
  [Status.Skipped]: "Skipped",
  [Status.CloseRequired]: "Close required",
  [Status.Failed]: "Failed",
};

const SEVERITY_RANK: Array<AlertColor> = ["success", "info", "warning", "error"];

function statusColor(status?: Status | null): AlertColor {
  return (status && STATUS_COLOR[status]) || "info";
}

function statusLabel(status?: Status | null): string {
  return (status && STATUS_LABEL[status]) || status || "Unknown";
}

export function needsClosing(outcomes: Array<AlignmentOutcome>): Array<AlignmentOutcome> {
  return outcomes.filter((outcome) => outcome.status === Status.CloseRequired);
}

export function worstSeverity(outcomes: Array<AlignmentOutcome>): AlertColor {
  return outcomes.reduce<AlertColor>((worst, outcome) => {
    const severity = statusColor(outcome.status);
    return SEVERITY_RANK.indexOf(severity) > SEVERITY_RANK.indexOf(worst) ? severity : worst;
  }, "success");
}

export const OUTCOMES_NOTE =
  "An applied outcome means OpenSearch accepted what was sent to it, not that the index matches the model.";

export const NOTHING_TO_ALIGN = "No current index uses it, so there was nothing to align.";

export function outcomesTitle(outcomes: Array<AlignmentOutcome>): string {
  return outcomes.length === 1 ? "Outcome of the alignment" : `Outcome for ${outcomes.length} indexes`;
}

/** One row per index, shared by the panel and the modal that present an outcome. */
export function OutcomesTable({ outcomes }: { outcomes: Array<AlignmentOutcome> }) {
  return (
    <TableContainer component={Paper} sx={{ overflowX: "auto" }}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Status</TableCell>
            <TableCell>Index</TableCell>
            <TableCell>Reason</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {outcomes.map((outcome, index) => (
            <TableRow key={`${outcome.dataIndexId ?? "unknown"}-${index}`}>
              <TableCell>
                <Chip size="small" color={statusColor(outcome.status)} label={statusLabel(outcome.status)} />
              </TableCell>
              <TableCell>
                {outcome.dataIndexId ? (
                  <MuiLink
                    component={RouterLink}
                    to={`/dataindex/${outcome.dataIndexId}/mode/view`}
                    underline="hover"
                    color="primary"
                  >
                    {outcome.indexName || `Data index ${outcome.dataIndexId}`}
                  </MuiLink>
                ) : (
                  <Box component="span">{outcome.indexName || "—"}</Box>
                )}
              </TableCell>
              {/* an OpenSearch refusal is often one long unbroken token */}
              <TableCell sx={{ whiteSpace: "pre-wrap", overflowWrap: "anywhere", maxWidth: 420 }}>
                {outcome.reason || "—"}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
