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
import { ApolloError } from "@apollo/client";
import { ModalConfirm } from "@components/Form";
import { Box } from "@mui/material";
import React from "react";
import { AlignmentOutcome, needsClosing } from "./AlignmentOutcomes";

/** The reason OpenSearch gave, when the refusal carries one. */
function errorMessage(caught: unknown): string {
  if (caught instanceof ApolloError) {
    return caught.graphQLErrors[0]?.message || caught.message;
  }
  if (caught instanceof Error) {
    return caught.message;
  }
  return "Unknown error";
}

/**
 * Runs an alignment without allowing the backend to close anything, and asks
 * before running it again with that permission.
 *
 * The backend tries the open index first and a refusal applies nothing, so the
 * question costs nothing and, unlike a blanket warning, it can name the indexes
 * that will actually stop serving.
 */
export function useAlignmentRun({
  run,
  onApplied,
}: {
  run(closeIfNeeded: boolean): Promise<Array<AlignmentOutcome>>;
  onApplied?(outcomes: Array<AlignmentOutcome>): void;
}) {
  const [outcomes, setOutcomes] = React.useState<Array<AlignmentOutcome> | null>(null);
  const [error, setError] = React.useState<string | null>(null);
  const [isRunning, setIsRunning] = React.useState(false);
  const [toClose, setToClose] = React.useState<Array<AlignmentOutcome> | null>(null);

  const execute = React.useCallback(
    async (closeIfNeeded: boolean) => {
      setIsRunning(true);
      setError(null);
      // a result already on screen belongs to the previous run, and the second
      // leg of a confirmed closing would otherwise flash it while it waits
      setOutcomes(null);

      try {
        const result = await run(closeIfNeeded);
        setOutcomes(result);

        const closing = needsClosing(result);

        if (closing.length > 0) {
          setToClose(closing);
        } else if (onApplied) {
          onApplied(result);
        }
      } catch (caught: unknown) {
        setOutcomes(null);
        setError(errorMessage(caught));
      } finally {
        setIsRunning(false);
      }
    },
    [run, onApplied],
  );

  // the guard is not `disabled`, which would move the focus away mid-action
  const start = React.useCallback(() => {
    if (isRunning) return;
    void execute(false);
  }, [execute, isRunning]);

  const dismiss = React.useCallback(() => {
    setOutcomes(null);
    setError(null);
  }, []);

  const ConfirmClosing = toClose ? (
    <ModalConfirm
      title="Close the index to apply it?"
      labelConfirm="Close and apply"
      type="warning"
      maxWidth="sm"
      fullWidth
      body="While it is closed an index is neither searchable nor writable. Nothing has been written to it yet."
      close={() => {
        setToClose(null);
        // the run was declined: its outcome is not a result to present
        setOutcomes(null);
        setError(null);
      }}
      actionConfirm={() => {
        setToClose(null);
        void execute(true);
      }}
    >
      <Box component="span" sx={{ display: "block", mt: 1 }}>
        {toClose.map((outcome) => (
          <Box component="span" key={String(outcome.dataIndexId)} sx={{ display: "block", fontWeight: "bold" }}>
            {outcome.indexName}
          </Box>
        ))}
      </Box>
    </ModalConfirm>
  ) : null;

  // isAsking lets a caller that presents the outcome as a modal hold it back
  // until the closing question has an answer, instead of stacking two dialogs
  return { outcomes, error, isRunning, start, dismiss, isAsking: toClose !== null, ConfirmClosing };
}
