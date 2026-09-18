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
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@mui/material";
import React from "react";
import { Link } from "react-router-dom";

// the status is typed as a string and not as the generated enum: a string enum
// member is assignable to string, so both callers can pass what they get back
export type AlignmentOutcome = {
  dataIndexId?: string | number | null;
  indexName?: string | null;
  status?: string | null;
  reason?: string | null;
};

const STATUS_COLOR: Record<string, AlertColor> = {
  APPLIED: "success",
  TEMPLATE_ONLY: "info",
  SKIPPED: "warning",
  CLOSE_REQUIRED: "warning",
  FAILED: "error",
};

const STATUS_LABEL: Record<string, string> = {
  APPLIED: "Applied",
  TEMPLATE_ONLY: "Template only",
  SKIPPED: "Skipped",
  CLOSE_REQUIRED: "Close required",
  FAILED: "Failed",
};

const SEVERITY_RANK: Array<AlertColor> = ["success", "info", "warning", "error"];

export function statusColor(status?: string | null): AlertColor {
  return (status && STATUS_COLOR[status]) || "info";
}

export function statusLabel(status?: string | null): string {
  return (status && STATUS_LABEL[status]) || status || "Unknown";
}

export function needsClosing(outcomes: Array<AlignmentOutcome>): Array<AlignmentOutcome> {
  return outcomes.filter((outcome) => outcome.status === "CLOSE_REQUIRED");
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
                  <Link to={`/dataindex/${outcome.dataIndexId}/mode/view`}>{outcome.indexName}</Link>
                ) : (
                  <Box component="span">{outcome.indexName}</Box>
                )}
              </TableCell>
              <TableCell sx={{ whiteSpace: "pre-wrap" }}>{outcome.reason || "—"}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
