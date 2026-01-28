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
import { ModalConfirm } from "@components/Form/Modals/ModalConfirm";
import React from "react";

interface UseConfirmModalProps {
  title: string;
  body: string;
  labelConfirm: string;
}

export const useConfirmModal = ({ title, body, labelConfirm }: UseConfirmModalProps) => {
  const [isOpen, setIsOpen] = React.useState(false);
  const [resolvePromise, setResolvePromise] = React.useState<(value: boolean) => void>();

  const openConfirmModal = () => {
    setIsOpen(true);
    return new Promise<boolean>((resolve) => {
      setResolvePromise(() => resolve);
    });
  };

  const handleConfirm = () => {
    resolvePromise?.(true);
    setIsOpen(false);
  };

  const handleClose = () => {
    resolvePromise?.(false);
    setIsOpen(false);
  };

  const ConfirmModal = () => (
    isOpen ? (
      <ModalConfirm
        title={title}
        body={body}
        labelConfirm={labelConfirm}
        actionConfirm={handleConfirm}
        close={handleClose}
      />
    ) : null
  );

  return { openConfirmModal, ConfirmModal };
}; 
