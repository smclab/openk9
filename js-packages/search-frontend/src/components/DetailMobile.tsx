import React from "react";
import { GenericResultItem, DetailRendererProps } from "./client";
import "overlayscrollbars/css/OverlayScrollbars.css";
import { DetailMemo } from "./Detail";
import { ModalDetail } from "./ModalDetail";
import { useTranslation } from "react-i18next";
import { TemplatesProps } from "../embeddable/entry";

export type DetailMobileProps<E> = {
  result: GenericResultItem<E> | null;
  setDetailMobile: any;
  onClose(): void;
  cardDetailsOnOver: boolean;
  template: TemplatesProps | null;
};

function DetailMobile<E>(props: DetailMobileProps<E>) {
  const result = props.result as any;
  const setDetailMobile = props.setDetailMobile as any;
  const action = props.onClose;
  const cardDetailsOnOver = props.cardDetailsOnOver;
  const template = props.template;
  const modalRef = React.useRef(null);
  const [isOpen, setIsOpen] = React.useState(true);
  const [isViewButton, setIsViewButton] = React.useState(false);

  const { t } = useTranslation();

  React.useEffect(() => {
    const modalElement = modalRef.current as any;

    if (modalElement) {
      const focusableElements = modalElement?.querySelectorAll(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])',
      );
      const firstElement = focusableElements[0];
      const lastElement = focusableElements[focusableElements.length - 1];

      const handleTabKeyPress = (event: any) => {
        if (event.key === "Tab") {
          if (event.shiftKey && document.activeElement === firstElement) {
            event.preventDefault();
            lastElement.focus();
          } else if (
            !event.shiftKey &&
            document.activeElement === lastElement
          ) {
            event.preventDefault();
            firstElement.focus();
          }
        }
      };

      const handleEscapeKeyPress = (event: any) => {
        if (event.key === "Escape") {
          setIsOpen(false);
        }
      };

      modalElement?.addEventListener("keydown", handleTabKeyPress);
      modalElement?.addEventListener("keydown", handleEscapeKeyPress);

      return () => {
        modalElement?.removeEventListener("keydown", handleTabKeyPress);
        modalElement?.removeEventListener("keydown", handleEscapeKeyPress);
      };
    }
  }, [isOpen, setIsOpen, result]);

  const componet = (
    <div
      ref={modalRef}
      style={{ height: "100%" }}
      id="dialog-modal-detail"
      aria-labelledby={t("detail-modal") || "detail modal"}
      role="dialog"
      className="modal"
      aria-modal={isOpen ? "true" : "false"}
    >
      <DetailMemo
        result={result}
        setDetailMobile={setDetailMobile}
        isMobile={true}
        actionOnCLose={action}
        cardDetailsOnOver={cardDetailsOnOver}
        setViewButtonDetail={setIsViewButton}
        viewButtonDetail={isViewButton}
        template={template}
      />
    </div>
  );

  if (!result) {
    document.body.style.overflow = "auto";
    return null;
  }
  document.body.style.overflow = "hidden";

  return <ModalDetail content={componet} />;
}
export const DetailMobileMemo = React.memo(DetailMobile);
