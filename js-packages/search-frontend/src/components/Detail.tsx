import React from "react";
import { css } from "styled-components/macro";
import { WebDetail } from "../renderers/openk9/web/WebDetail";
import { GenericResultItem, SidebarRendererProps } from "@openk9/rest-api";
import { DocumentDetail } from "../renderers/openk9/document/DocumentDetail";
import { PdfDetail } from "../renderers/openk9/pdf/PdfDetail";
import { useRenderers } from "./useRenderers";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import "overlayscrollbars/css/OverlayScrollbars.css";
import { Logo } from "./Logo";

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
      style={{
        position: "relative",
        width: "100%",
        height: "100%",
        boxSizing: "border-box",
        overflow: "auto",
      }}
    >
      <div
        css={css`
          position: absolute;
          width: 100%;
          box-sizing: border-box;
          padding: 8px 16px;
        `}
      >
        {(() => {
          const Renderer: React.FC<SidebarRendererProps<E>> =
            result.source.documentTypes
              .map((k: string) => renderers?.sidebarRenderers[k])
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
    <div
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
  );
}
