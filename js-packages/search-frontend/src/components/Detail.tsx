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

type DetailProps<E> = {
  result: GenericResultItem<E> | null;
};
function Detail<E>(props: DetailProps<E>) {
  const result = props.result as any;
  const renderers = useRenderers();
  if (!result) {
    return <NoDetail />;
  }
  return (
    <OverlayScrollbarsComponent
      className="openk9-detail-overlay-scrollbars-component"
      style={{
        position: "relative",
        width: "100%",
        height: "100%",
        boxSizing: "border-box",
        overflow: "auto",
      }}
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
        `}
      >
        <span>
          <PreviewSvg />
        </span>
        <span
          className="openk9-detail-title title"
          css={css`
            margin-left: 5px;
            font-style: normal;
            font-weight: 700;
            font-size: 18px;
            height: 18px;
            line-height: 22px;
            align-items: center;
            color: #3f3f46;
            margin-left: 8px;
          `}
        >
          Preview
        </span>
      </div>
      <div
        className="openk9-detail-container-card "
        css={css`
          position: absolute;
          width: 100%;
          box-sizing: border-box;
          padding: 8px 16px;
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
    </OverlayScrollbarsComponent>
  );
}
export const DetailMemo = React.memo(Detail);

function NoDetail() {
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
        `}
      >
        <span>
          <PreviewSvg />
        </span>
        <span
          className="openk9-no-detail-title title"
          css={css`
            margin-left: 5px;
            font-style: normal;
            font-weight: 700;
            font-size: 18px;
            height: 18px;
            line-height: 22px;
            align-items: center;
            color: #3f3f46;
            margin-left: 8px;
          `}
        >
          Preview
        </span>
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
        <h3>No details</h3>
        <div>Move the mouse over a result to see details about it</div>
      </div>
    </React.Fragment>
  );
}
