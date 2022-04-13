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

import React from "react";
import { createUseStyles } from "react-jss";
import clsx from "clsx";
import ClayModal, { useModal } from "@clayui/modal";
import { ThemeType } from "./theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  btnCloseModal: {
    marginRight: theme.spacingUnit * 2,
  },
}));

export function ConfirmationModal({
  title,
  message,
  onCloseModal,
  onConfirmModal,
  abortText,
  confirmText,
}: {
  title?: JSX.Element | string;
  message: JSX.Element | string;
  abortText?: JSX.Element | string;
  confirmText?: JSX.Element | string;
  onCloseModal(): void;
  onConfirmModal(): void;
}) {
  const classes = useStyles();

  const { observer, onClose } = useModal({
    onClose() {
      onCloseModal();
    },
  });

  function onConfirm() {
    onConfirmModal();
    onClose();
  }

  return (
    <ClayModal observer={observer} size="lg">
      {title && <ClayModal.Header>{title}</ClayModal.Header>}
      <ClayModal.Body>{message}</ClayModal.Body>
      <ClayModal.Footer
        last={
          <>
            <button
              className={clsx("btn btn-secondary", classes.btnCloseModal)}
              type="button"
              onClick={onClose}
            >
              {abortText || "Close"}
            </button>
            <button
              className="btn btn-primary"
              type="button"
              onClick={onConfirm}
            >
              {confirmText || "Confirm"}
            </button>
          </>
        }
      />
    </ClayModal>
  );
}
