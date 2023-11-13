import React from "react";
import { css } from "styled-components/macro";
import { WebDetail } from "../renderers/openk9/web/WebDetail";
import { GenericResultItem, DetailRendererProps } from "./client";
import { DocumentDetail } from "../renderers/openk9/document/DocumentDetail";
import { PdfDetail } from "../renderers/openk9/pdf/PdfDetail";
import { useRenderers } from "./useRenderers";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import "overlayscrollbars/css/OverlayScrollbars.css";
import { Logo } from "./Logo";
import { ResultSvg } from "../svgElement/ResultSvg";
import { PreviewSvg } from "../svgElement/PreviewSvg";
import { DeleteLogo } from "./DeleteLogo";
import { useTranslation } from "react-i18next";

export type DetailProps<E> = {
  result: GenericResultItem<E> | null;
  setDetailMobile?: any;
  isMobile?:boolean;
  actionOnCLose():void;
};
function Detail<E>(props: DetailProps<E>) {
  const result = props.result as any;
  const setDetailMobile = props.setDetailMobile;
  const actionOnCLose=props.actionOnCLose;
  const renderers = useRenderers();
  const isMobile= props.isMobile;
  const refFocus=React.useRef<HTMLButtonElement>(null);
  const { t } = useTranslation();

  const scrollContainer = document.querySelector(
    ".openk9-detail-overlay-scrollbars-component",
  );

  if (scrollContainer) {
    scrollContainer.scrollTop = 0;
  }
  if (!result) {
    return <NoDetail />;
  }
  if(isMobile && isMobile===true){
    refFocus.current?.focus()
  }
  return (
    <div
      className="openk9-detail-overlay-scrollbars-component"
      css={css`
        position: relative;
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        overflow: auto;
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
          css={css`
            display: flex;
            gap: 5px;
          `}
        >
          <div>
            <PreviewSvg />
          </div>
          <h2
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
      <div
        className="openk9-detail-container-card "
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
    </div>
  );
}
export const DetailMemo = React.memo(Detail);

export function NoDetail() {
  const { t } = useTranslation();
  return (
    <React.Fragment>
      <div
        className="openk9-no-detail-container-title box-title"
        css={css`
          padding: 0px 16px;
          width: 100%;
          background: #fafafa;
          padding-top: 17px;
          padding-bottom: 8px;
          display: flex;
          gap: 3px;
        `}
      >
        <div>
          <PreviewSvg />
        </div>
        <div className="openk9-no-detail-title title">
          <h2
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
      </div>
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
        <Logo size={128} />

        <h3>{t("no-details")}</h3>
        <div>{t("no-result")}</div>
      </div>
    </React.Fragment>
  );
}
