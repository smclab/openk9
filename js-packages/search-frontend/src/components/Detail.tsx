import React from "react";
import { css } from "styled-components/macro";
import { WebDetail } from "../renderers/openk9/web/WebDetail";
import { GenericResultItem, DetailRendererProps } from "./client";
import { DocumentDetail } from "../renderers/openk9/document/DocumentDetail";
import { PdfDetail } from "../renderers/openk9/pdf/PdfDetail";
import { useRenderers } from "./useRenderers";
import { PreviewSvg } from "../svgElement/PreviewSvg";
import { DeleteLogo } from "./DeleteLogo";
import { useTranslation } from "react-i18next";

export type DetailProps<E> = {
  result: GenericResultItem<E> | null;
  setDetailMobile?: any;
  isMobile?: boolean;
  cardDetailsOnOver: boolean;
  actionOnCLose(): void;
  callbackFocusedButton?(): void;
  setViewButtonDetail: React.Dispatch<React.SetStateAction<boolean>>;
  viewButtonDetail: boolean;
};
function Detail<E>(props: DetailProps<E>) {
  const result = props.result as any;
  const setDetailMobile = props.setDetailMobile;
  const actionOnCLose = props.actionOnCLose;
  const renderers = useRenderers();
  const isMobile = props.isMobile;
  const setViewButtonDetail = props.setViewButtonDetail;
  const viewButtonDetail = props.viewButtonDetail;
  const callbackFocusedButton = props.callbackFocusedButton;
  const cardDetailsOnOver = props.cardDetailsOnOver;
  const [showButton, setShowButton] = React.useState(false);

  const modalRef = React.useRef(null);

  React.useEffect(() => {
    const modalElement = modalRef.current as any;

    if (modalElement) {
      const focusableElements = modalElement?.querySelectorAll(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])',
      );
      const firstElement = focusableElements[0];
      const lastElement = focusableElements[focusableElements.length - 1];
      const trapForInvisibleButton = focusableElements[1];

      const handleTabKeyPress = (event: any) => {
        if (event.key === "Tab") {
          if (document.activeElement === firstElement) {
            setShowButton(true);
          }
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

      const handleEscapeKeyPress = (event: any) => {};

      modalElement?.addEventListener("keydown", handleTabKeyPress);
      modalElement?.addEventListener("keydown", handleEscapeKeyPress);

      return () => {
        modalElement?.removeEventListener("keydown", handleTabKeyPress);
        modalElement?.removeEventListener("keydown", handleEscapeKeyPress);
      };
    }
  }, [result]);

  React.useEffect(() => {
    const modalElement = modalRef.current as any;

    const isFocusWithinModal = modalElement?.contains(document.activeElement);
    if (isFocusWithinModal) {
      setShowButton(true);
    } else {
      setShowButton(false);
    }
  }, [document.activeElement]);

  const refFocus = React.useRef<HTMLButtonElement>(null);
  const { t } = useTranslation();

  const scrollContainer = document.querySelector(
    ".openk9-detail-overlay-scrollbars-component",
  );

  React.useEffect(() => {
    if (isMobile && isMobile === true) {
      refFocus.current?.focus();
    }
    if (scrollContainer) {
      scrollContainer.scrollTop = 0;
    }
  }, []);

  return (
    <div
      role="region"
      ref={modalRef}
      aria-labelledby="title-preview-openk9"
      className="openk9-detail-overlay-scrollbars-component"
      css={css`
        position: relative;
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        overflow: auto;
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
    background-color: transparent;
}
 
::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.4); 
  border-radius: 10px;
  height:5px;  
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, .55);
  height:5px;
}
      `}
    >
      <div
        className="openk9-detail-container-title box-title"
        css={css`
          padding: 0px 16px;
          width: 100%;
          background: #fafafa;
          padding-top: 17px;
          padding-bottom: 8px;
          display: flex;
          gap: 3px;
          box-sizing: border-box;
          justify-content: space-between;
          border-top-left-radius: ${setDetailMobile ? "20px" : "0px"};
          border-top-right-radius: ${setDetailMobile ? "20px" : "0px"};
        `}
      >
        <div
          className="openk9-icon-and-title-detail"
          css={css`
            display: flex;
            gap: 5px;
          `}
        >
          <div>
            <PreviewSvg />
          </div>
          <h2
            id="title-preview-openk9"
            tabIndex={0}
            className="openk9-detail-class-title"
            css={css`
              font-style: normal;
              font-weight: 700;
              font-size: 18px;
              height: 18px;
              line-height: 22px;
              align-items: center;
              color: #3f3f46;
              margin: 0;
            `}
          >
            {t("preview")}
          </h2>
        </div>
        {showButton && !isMobile && viewButtonDetail && (
          <button
            className="button-return-cards"
            css={css`
              border: none;
              background: #f3e2e6;
              color: var(--openk9-embeddable-search--primary-color);
              font-weight: 600;
              border-radius: 5px;
              cursor: pointer;
              &:focus-visible {
                box-shadow: 0 0 0 0.125rem #fff, 0 0 0 0.25rem #ee4848;
                outline: 0;
              }
            `}
            onClick={() => {
              if (callbackFocusedButton) callbackFocusedButton();
              setViewButtonDetail(false);
            }}
          >
            {t("return-cards")}
          </button>
        )}
        {setDetailMobile && (
          <button
            aria-label={t("close") || "close"}
            ref={refFocus}
            css={css`
              cursor: pointer;
              background: inherit;
              border: none;
            `}
            onClick={() => {
              setDetailMobile(null);
              actionOnCLose();
            }}
          >
            <DeleteLogo />
          </button>
        )}
      </div>
      {result ? (
        <div
          className="openk9-detail-container-card button-start-detail"
          css={css`
            position: absolute;
            width: 100%;
            box-sizing: border-box;
            padding: 8px 16px;
            background: white;
            border-bottom-left-radius: ${setDetailMobile ? "20px" : "0px"};
            border-bottom-right-radius: ${setDetailMobile ? "20px" : "0px"};
          `}
        >
          {(() => {
            const Renderer: React.FC<DetailRendererProps<E>> =
              result.source.documentTypes
                .map((k: string) => renderers?.detailRenderers[k])
                .find(Boolean);
            if (Renderer) {
              return <Renderer result={result} />;
            }
            if (result.source.documentTypes.includes("pdf")) {
              return <PdfDetail result={result} />;
            }
            if (result.source.documentTypes.includes("document")) {
              return <DocumentDetail result={result} />;
            }
            if (result.source.documentTypes.includes("web")) {
              return <WebDetail result={result} />;
            }
            return <pre css={css``}>{JSON.stringify(result, null, 2)}</pre>;
          })()}
        </div>
      ) : (
        <div
          className="openk9-no-detail-content "
          css={css`
            color: var(--openk9-embeddable-search--secondary-text-color);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100%;
          `}
        >
          <h3>{t("no-details")}</h3>
          <div>{cardDetailsOnOver ? t("no-result") : t("no-result-click")}</div>
        </div>
      )}
    </div>
  );
}
export const DetailMemo = React.memo(Detail);
