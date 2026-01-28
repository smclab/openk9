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
import { GenericResultItem, DetailRendererProps } from "./client";
import "overlayscrollbars/css/OverlayScrollbars.css";
import { DetailMemo } from "./Detail";
import { ModalDetail } from "./ModalDetail";
import { useTranslation } from "react-i18next";
import { TemplatesProps } from "../embeddable/entry";
import { css } from "styled-components";

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
      css={css`
        height: 100%;
      `}
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

