import React from "react";
import { css } from "styled-components/macro";
import { WebResult } from "../renderers/openk9/web/WebResult";
import { GenericResultItem, ResultRendererProps } from "./client";
import { DocumentResult } from "../renderers/openk9/document/DocumentResult";
import { PdfResult } from "../renderers/openk9/pdf/PdfResult";
import { Renderers } from "./useRenderers";

type ResultProps<E> = {
  renderers: Renderers;
  result: GenericResultItem<E>;
  onDetail(result: GenericResultItem<E> | null): void;
};
function Result<E>(props: ResultProps<E>) {
  const result = props.result as any;
  const { onDetail, renderers } = props;
  return (
    <div
      className="openk9-embeddable-search--result-container"
      onMouseEnter={() => onDetail(result)}
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
          return <PdfResult result={result} />;
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
export const ResultMemo = React.memo(Result) as typeof Result;
