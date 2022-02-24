import React from "react";
import { css } from "styled-components/macro";
import { WebDetail } from "../renderers/openk9/web/WebDetail";
import { GenericResultItem, SidebarRendererProps } from "@openk9/rest-api";
import { DocumentDetail } from "../renderers/openk9/document/DocumentDetail";
import { PdfDetail } from "../renderers/openk9/pdf/PdfDetail";
i
import { Renderers } from "./useRenderers";

type DetailProps<E> = {
  renderers: Renderers;
  result: GenericResultItem<E>;
};
function Detail<E>(props: DetailProps<E>) {
  const result = props.result as any;
  const { renderers } = props;
  return (
    <div
      css={css`
        position: relative;
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        overflow: auto;
      `}
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
    </div>
  );
}
export const DetailMemo = React.memo(Detail);
