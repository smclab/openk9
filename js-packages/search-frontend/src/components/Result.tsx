import React from "react";
import { css } from "styled-components/macro";
import { WebResult } from "../renderers/openk9/web/WebResult";
import { GenericResultItem, ResultRendererProps } from "./client";
import { DocumentResult } from "../renderers/openk9/document/DocumentResult";
import { PdfResult } from "../renderers/openk9/pdf/PdfResult";
import { Renderers } from "./useRenderers";
import { MobileLogoSvg } from "../svgElement/MobileLogoSvg";
import { useTranslation } from "react-i18next";

type ResultProps<E> = {
  renderers: Renderers;
  result: GenericResultItem<E>;
  onDetail(result: GenericResultItem<E> | null): void;
  isMobile: boolean;
  setDetailMobile(result: GenericResultItem<E> | null): void;
  overChangeCard: boolean;
  setIdPreview?:
    | React.Dispatch<React.SetStateAction<string>>
    | undefined
    | null;
};
function Result<E>(props: ResultProps<E>) {
  const result = props.result as any;
  const { onDetail, renderers } = props;
  const isMobile = props.isMobile;
  const setIdPreview= props.setIdPreview;
  const setDetailMobile = props.setDetailMobile;
  const overChangeCard = props.overChangeCard;
  return (
    <div
      className="openk9-embeddable-search--result-container"
      onMouseEnter={() => overChangeCard && !isMobile && onDetail(result)}
      onClick={() => !overChangeCard && !isMobile && onDetail(result)}
      css={css`
        cursor: ${!overChangeCard ? "pointer" : "auto"};
      `}
    >
      {(() => {
        const Renderer: React.FC<ResultRendererProps<E>> =
          result.source.documentTypes
            .map((k: string) => renderers?.resultRenderers[k])
            .find(Boolean);
        if (Renderer) {
          return (
            <React.Fragment>
              <Renderer result={result} />
              {isMobile &&
                CreateButton({
                  setIdPreview,
                  setDetailMobile,
                  result,
                })}
            </React.Fragment>
          );
        }
        if (result.source.documentTypes.includes("pdf")) {
          return (
            <React.Fragment>
              <PdfResult result={result} />
              {isMobile &&
                CreateButton({
                  setIdPreview,
                  setDetailMobile,
                  result,
                })}
            </React.Fragment>
          );
        }
        if (result.source.documentTypes.includes("document")) {
          return (
            <React.Fragment>
              <DocumentResult result={result} />
              {isMobile &&
                CreateButton({
                  setIdPreview,
                  setDetailMobile,
                  result,
                })}
            </React.Fragment>
          );
        }
        if (result.source.documentTypes.includes("web")) {
          return (
            <React.Fragment>
              <WebResult result={result} />
              {isMobile &&
                CreateButton({
                  setIdPreview,
                  setDetailMobile,
                  result,
                })}
            </React.Fragment>
          );
        }
        return (
          <pre
            className="openk9-embeddable-search--no-result-container"
            css={css`
              height: 100px;
              overflow: hidden;
            `}
          >
            Not implemented
            {JSON.stringify(result, null, 2)}
          </pre>
        );
      })()}
    </div>
  );
}
function CreateButton({
  setDetailMobile,
  result,
  setIdPreview,
}: {
  setDetailMobile: (result: GenericResultItem<any> | null) => void;
  result: GenericResultItem<any>;
  setIdPreview?: React.Dispatch<React.SetStateAction<string>>|undefined|null;
}) {
  const { t } = useTranslation();
  return (
    <div
      className="openk9-wrapper-button-mobile"
      css={css`
        padding: 24px;
        padding-top: 8px;
        @media (max-width: 480px) {
          padding: 20px;
          padding-top: 4px;
        }
      `}
    >
      <button
        id={"preview-card-"+result?.source?.id}
        css={css`
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 3px;
          padding: 8px 16px;
          background-color: #f9edee;
          border: 1px solid #f9edee;
          border-radius: 50px;
          color: #c0272b;
          font-weight: 500;
          font-size: 13px;
          cursor: pointer;
        `}
        onClick={() => {
          if(setIdPreview)
          setIdPreview(result?.source?.id||"")
          setDetailMobile(result);
        }}
      >
        <span> {t("preview")}</span>
        <MobileLogoSvg />
      </button>
    </div>
  );
}

export const ResultMemo = React.memo(Result) as typeof Result;
