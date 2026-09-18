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
import { ModalConfirm } from "@components/Form";
import { Box } from "@mui/material";
import React from "react";
import {
  AlignmentOutcome,
  NOTHING_TO_ALIGN,
  OUTCOMES_NOTE,
  OutcomesTable,
  outcomesTitle,
  worstSeverity,
} from "./AlignmentOutcomes";

// an outcome is read, not decided: the button only dismisses, which the modal
// already does by itself
const nothingToConfirm = () => undefined;

/**
 * The outcome of an alignment, in the modal the rest of the admin uses.
 *
 * It is a modal and not a toast on purpose: three of the five statuses carry
 * something to read, a FAILED one carries the raw refusal of OpenSearch, and a
 * toast takes all of that away after six seconds. It is not a panel either,
 * because an outcome is read once and dismissed, and a panel pushes down the
 * very thing the outcome is about.
 */
export function AlignmentOutcomesModal({
  outcomes,
  error,
  onClose,
  title,
  note = OUTCOMES_NOTE,
  emptyMessage = NOTHING_TO_ALIGN,
}: {
  outcomes: Array<AlignmentOutcome> | null;
  error?: string | null;
  onClose(): void;
  title?: string;
  note?: string;
  emptyMessage?: string;
}) {
  if (error) {
    return (
      <ModalConfirm
        title="The request was refused"
        type="error"
        labelConfirm="Close"
        hideCancel
        maxWidth="md"
        fullWidth
        body={error}
        close={onClose}
        actionConfirm={nothingToConfirm}
      />
    );
  }

  if (!outcomes) return null;

  if (outcomes.length === 0) {
    return (
      <ModalConfirm
        title="Nothing to align"
        type="info"
        labelConfirm="Close"
        hideCancel
        maxWidth="sm"
        fullWidth
        body={emptyMessage}
        close={onClose}
        actionConfirm={nothingToConfirm}
      />
    );
  }

  return (
    <ModalConfirm
      title={title ?? outcomesTitle(outcomes)}
      type={worstSeverity(outcomes)}
      labelConfirm="Close"
      hideCancel
      maxWidth="md"
      fullWidth
      body={note}
      close={onClose}
      actionConfirm={nothingToConfirm}
    >
      <Box component="span" sx={{ display: "block", mt: 2 }}>
        <OutcomesTable outcomes={outcomes} />
      </Box>
    </ModalConfirm>
  );
}
