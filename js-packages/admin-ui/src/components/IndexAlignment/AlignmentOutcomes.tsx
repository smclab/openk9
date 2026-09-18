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
  Alert,
  AlertColor,
  AlertTitle,
  Box,
  Chip,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
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

function worstSeverity(outcomes: Array<AlignmentOutcome>): AlertColor {
  return outcomes.reduce<AlertColor>((worst, outcome) => {
    const severity = statusColor(outcome.status);
    return SEVERITY_RANK.indexOf(severity) > SEVERITY_RANK.indexOf(worst) ? severity : worst;
  }, "success");
}

/**
 * The outcome of an alignment, one row per index.
 *
 * It is an Alert and not a toast on purpose: three of the five statuses carry
 * something to read, a FAILED one carries the raw refusal of OpenSearch, and a
 * toast takes all of that away after six seconds.
 */
export function AlignmentOutcomes({
  outcomes,
  error,
  title,
  note = "An applied outcome means OpenSearch accepted what was sent to it, not that the index matches the model.",
  emptyMessage = "No current index uses it, so there was nothing to align.",
}: {
  outcomes: Array<AlignmentOutcome> | null;
  error?: string | null;
  title?: string;
  note?: string;
  emptyMessage?: string;
}) {
  if (error) {
    return (
      <Alert severity="error" sx={{ mt: 2 }}>
        <AlertTitle>The request was refused</AlertTitle>
        {error}
      </Alert>
    );
  }

  if (!outcomes) return null;

  if (outcomes.length === 0) {
    return (
      <Alert severity="info" sx={{ mt: 2 }}>
        <AlertTitle>Nothing to align</AlertTitle>
        {emptyMessage}
      </Alert>
    );
  }

  return (
    <Alert severity={worstSeverity(outcomes)} sx={{ mt: 2 }}>
      <AlertTitle>
        {title ?? (outcomes.length === 1 ? "Outcome of the alignment" : `Outcome for ${outcomes.length} indexes`)}
      </AlertTitle>
      <Typography variant="body2" sx={{ mb: 1 }}>
        {note}
      </Typography>
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
    </Alert>
  );
}
