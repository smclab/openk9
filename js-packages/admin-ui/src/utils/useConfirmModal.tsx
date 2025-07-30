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