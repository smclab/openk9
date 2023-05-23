import React from "react";
import { css } from "styled-components/macro";
import { WebResult } from "../renderers/openk9/web/WebResult";
import { GenericResultItem, ResultRendererProps } from "./client";
import { DocumentResult } from "../renderers/openk9/document/DocumentResult";
import { PdfResult } from "../renderers/openk9/pdf/PdfResult";
import { Renderers } from "./useRenderers";
import { openk9 } from "../App";
import { PreviewSvg } from "../svgElement/PreviewSvg";
import { DeleteLogo } from "./DeleteLogo";

type ResultProps<E> = {
  renderers: Renderers;
  result: GenericResultItem<E>;
  onDetail(result: GenericResultItem<E> | null): void;
  isMobile: boolean;
  setDetailMobile(result: GenericResultItem<E> | null): void;
};
function Result<E>(props: ResultProps<E>) {
  const result = props.result as any;
  const { onDetail, renderers } = props;
  const isMobile = props.isMobile;
  const setDetailMobile = props.setDetailMobile;
  return (
    <div
      className="openk9-embeddable-search--result-container"
      onMouseEnter={() => !isMobile && onDetail(result)}
    >
      {(() => {
        const Renderer: React.FC<ResultRendererProps<E>> =
          result.source.documentTypes
            .map((k: string) => renderers?.resultRenderers[k])
            .find(Boolean);
        if (Renderer) {
          return <Renderer result={result} />;
        }
        if (result.source.documentTypes.includes("pdf")) {
          return (
            <React.Fragment>
              <PdfResult result={result} />
              {isMobile &&
                CreateButton({
                  setDetailMobile,
                  result,
                })}
            </React.Fragment>
          );
        }
        if (result.source.documentTypes.includes("document")) {
          return <DocumentResult result={result} />;
        }
        if (result.source.documentTypes.includes("web")) {
          return <WebResult result={result} />;
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
}: {
  setDetailMobile: (result: GenericResultItem<any> | null) => void;
  result: any;
}) {
  return (
    <div
      css={css`
        padding-left: 16px;
        padding-bottom: 8px;
      `}
    >
      <div
        css={css`
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 4px 8px;
          gap: 4px;
          height: 21px;
          width: 108px;
          background: #ffffff;
          border: 1px solid #f9edee;
          background: #f9edee;
          border-radius: 20px;
          cursor: pointer;
          white-space: nowrap;
        `}
        onClick={() => {
          setDetailMobile(result);
        }}
      >
        <div
          css={css`
            color: red;
            margin-bottom: 14px;
            font-weight: 500;
            font-size: 13px;
            display: block;
            margin-block-start: 1em;
            margin-block-end: 1em;
            margin-inline-start: 0px;
            margin-inline-end: 0px;
          `}
        >
          <div
            css={css`
              display: flex;
              align-items: baseline;
              color: #c0272b;
            `}
          >
            Detail{" "}
          </div>
        </div>
      </div>
    </div>
  );
}

export const ResultMemo = React.memo(Result) as typeof Result;
