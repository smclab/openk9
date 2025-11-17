import React from "react";
import { css } from "styled-components";
import { WebResult } from "../renderers/openk9/web/WebResult";
import { GenericResultItem, ResultRendererProps } from "./client";
import { DocumentResult } from "../renderers/openk9/document/DocumentResult";
import { PdfResult } from "../renderers/openk9/pdf/PdfResult";
import { Renderers } from "./useRenderers";
import { MobileLogoSvg } from "../svgElement/MobileLogoSvg";
import { useTranslation } from "react-i18next";
import { TemplatesProps } from "../embeddable/entry";

type ResultProps<E> = {
  renderers: Renderers;
  templateCustom?: TemplatesProps | null;
  result: GenericResultItem<E>;
  onDetail(result: GenericResultItem<E> | null): void;
  isMobile: boolean;
  setDetailMobile(result: GenericResultItem<E> | null): void;
  overChangeCard: boolean;
  viewButton?: boolean;
  setViewButtonDetail?: React.Dispatch<React.SetStateAction<boolean>>;
  setIdPreview?:
    | React.Dispatch<React.SetStateAction<string>>
    | undefined
    | null;
};

function Result<E>(props: ResultProps<E>) {
  const result = props.result as any;
  const {
    onDetail,
    renderers,
    isMobile,
    setIdPreview,
    setDetailMobile,
    viewButton,
    overChangeCard,
    setViewButtonDetail,
    templateCustom,
  } = props;

  const getCustomTemplate = () => {
    if (!templateCustom || templateCustom.length === 0) return null;

    const matchedTemplate = templateCustom.find((template) =>
      result.source.documentTypes.includes(template.source),
    );

    return matchedTemplate?.Template ?? null;
  };
  return (
    <div
      className={`openk9-embeddable-search--result-container openk9-card-${result?.source?.id}`}
      onMouseEnter={() => {
        if (overChangeCard && !isMobile) {
          onDetail(result);
          if (setIdPreview) setIdPreview(result?.source?.id || "");
        }
      }}
      onClick={() => {
        if (!overChangeCard && !isMobile && !viewButton) {
          onDetail(result);
          if (setIdPreview) setIdPreview(result?.source?.id || "");
        }
      }}
      css={css`
        cursor: ${!overChangeCard ? "pointer" : "auto"};
      `}
    >
      {(() => {
        const CustomTemplate = getCustomTemplate();

        if (CustomTemplate) {
          return (
            <React.Fragment>
              <CustomTemplate {...result} />
              {isMobile && (
                <CreateButton
                  setIdPreview={setIdPreview}
                  setDetailMobile={setDetailMobile}
                  result={result}
                />
              )}
            </React.Fragment>
          );
        }

        const Renderer: React.FC<ResultRendererProps<E>> =
          result.source.documentTypes
            .map((k: string) => renderers?.resultRenderers[k])
            .find(Boolean);

        if (Renderer && typeof Renderer === "function") {
          return (
            <React.Fragment>
              <Renderer result={result} />
              {isMobile && (
                <CreateButton
                  setIdPreview={setIdPreview}
                  setDetailMobile={setDetailMobile}
                  result={result}
                />
              )}
              {viewButton && !isMobile && (
                <ButtonDetail
                  result={result}
                  onDetail={onDetail}
                  setIdPreview={setIdPreview}
                  setViewButtonDetail={setViewButtonDetail}
                />
              )}
            </React.Fragment>
          );
        }

        if (result.source.documentTypes.includes("pdf")) {
          return (
            <React.Fragment>
              <PdfResult result={result} />
              {isMobile && (
                <CreateButton
                  setIdPreview={setIdPreview}
                  setDetailMobile={setDetailMobile}
                  result={result}
                />
              )}
            </React.Fragment>
          );
        }

        if (result.source.documentTypes.includes("document")) {
          return (
            <React.Fragment>
              <DocumentResult result={result} />
              {isMobile && (
                <CreateButton
                  setIdPreview={setIdPreview}
                  setDetailMobile={setDetailMobile}
                  result={result}
                />
              )}
              {viewButton && !isMobile && (
                <ButtonDetail
                  result={result}
                  onDetail={onDetail}
                  setIdPreview={setIdPreview}
                  setViewButtonDetail={setViewButtonDetail}
                />
              )}
            </React.Fragment>
          );
        }

        if (result.source.documentTypes.includes("web")) {
          return (
            <React.Fragment>
              <WebResult result={result} />
              {isMobile && (
                <CreateButton
                  setIdPreview={setIdPreview}
                  setDetailMobile={setDetailMobile}
                  result={result}
                />
              )}
              {viewButton && !isMobile && (
                <ButtonDetail
                  result={result}
                  onDetail={onDetail}
                  setIdPreview={setIdPreview}
                  setViewButtonDetail={setViewButtonDetail}
                />
              )}
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
  setIdPreview?:
    | React.Dispatch<React.SetStateAction<string>>
    | undefined
    | null;
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
        id={"preview-card-" + result?.source?.id}
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
          if (setIdPreview) setIdPreview(result?.source?.id || "");
          setDetailMobile(result);
        }}
      >
        <span>{t("preview")}</span>
        <MobileLogoSvg />
      </button>
    </div>
  );
}

function ButtonDetail<E>({
  result,
  onDetail,
  setIdPreview,
  setViewButtonDetail,
}: {
  result: GenericResultItem<any>;
  onDetail: (result: GenericResultItem<E> | null) => void;
  setIdPreview: React.Dispatch<React.SetStateAction<string>> | null | undefined;
  setViewButtonDetail?: React.Dispatch<React.SetStateAction<boolean>>;
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
        id={`openk9-button-card-${result?.source?.id}`}
        className="openk9-wrapper-button-mobile openk9-detail-web-button"
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
        onClick={(e) => {
          const recoveryButton = document.getElementById(
            "title-preview-openk9",
          ) as any;
          onDetail(result);
          if (e.screenX === 0 && e.screenY === 0) {
            setViewButtonDetail && setViewButtonDetail(true);
          } else {
            setViewButtonDetail && setViewButtonDetail(false);
          }
          if (setIdPreview) setIdPreview(result?.source?.id || "");
          if (recoveryButton) recoveryButton.focus();
        }}
      >
        <span>{t("preview")}</span>
        <MobileLogoSvg />
      </button>
    </div>
  );
}

export const ResultMemo = React.memo(Result) as typeof Result;
