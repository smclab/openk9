import React from "react";
import { css } from "styled-components/macro";
import { WebResult } from "./WebResult";
import { myTheme } from "../utils/myTheme";
import { DocumentResult } from "./DocumentResult";
import { NotizieResult } from "./bdi/NotizieResult";
import { PubblicazioniResult } from "./bdi/PubblicazioniResult";
import { PdfResult } from "./PdfResult";
import { MostreResult } from "./cm/MostreResult";
import { EventiResult } from "./cm/EventiResult";
import { PetizioniResult } from "./cm/PetizioniResult";
import { ProcessiResult } from "./cm/ProcessiResult";
import { GenericResultItem } from "@openk9/rest-api";

type ResultProps<E> = {
  result: GenericResultItem<E>;
  onDetail(result: GenericResultItem<E> | null): void;
};
function Result<E>({ result, onDetail }: ResultProps<E>) {
  return (
    <div
      css={css`
        padding: 0px 16px 16px 16px;
      `}
    >
      <div
        css={css`
          border: 1px solid ${myTheme.searchBarBorderColor};
          border-radius: 4px;
        `}
        onMouseEnter={() => onDetail(result)}
      >
        {(() => {
          const resultAny = result as any;
          if (resultAny.source.documentTypes.includes("notizie")) {
            return <NotizieResult result={resultAny} />;
          }
          if (resultAny.source.documentTypes.includes("pubblicazioni")) {
            return <PubblicazioniResult result={resultAny} />;
          }
          if (resultAny.source.documentTypes.includes("mostre")) {
            return <MostreResult result={resultAny} />;
          }
          if (resultAny.source.documentTypes.includes("eventi")) {
            return <EventiResult result={resultAny} />;
          }
          if (resultAny.source.documentTypes.includes("petizioni")) {
            return <PetizioniResult result={resultAny} />;
          }
          if (resultAny.source.documentTypes.includes("processi")) {
            return <ProcessiResult result={resultAny} />;
          }
          if (resultAny.source.documentTypes.includes("pdf")) {
            return <PdfResult result={resultAny} />;
          }
          if (resultAny.source.documentTypes.includes("document")) {
            return <DocumentResult result={resultAny} />;
          }
          if (resultAny.source.documentTypes.includes("web")) {
            return <WebResult result={resultAny} />;
          }
          return (
            <pre
              css={css`
                height: 100px;
                overflow: hidden;
              `}
            >
              {JSON.stringify(result, null, 2)}
            </pre>
          );
        })()}
      </div>
    </div>
  );
}
export const ResultMemo = React.memo(Result) as typeof Result;
