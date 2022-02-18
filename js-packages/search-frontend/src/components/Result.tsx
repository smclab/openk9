import React from "react";
import { css } from "styled-components/macro";
import { WebResult } from "../renderers/openk9/web/WebResult";
import { UserResult } from "../renderers/openk9/user/UserResult";
import { GenericResultItem, ResultRendererProps } from "@openk9/rest-api";
import { DocumentResult } from "../renderers/openk9/document/DocumentResult";
import { NotizieResult } from "../renderers/bdi/notizie/NotizieResult";
import { PubblicazioniResult } from "../renderers/bdi/pubblicazioni/PubblicazioniResult";
import { PdfResult } from "../renderers/openk9/pdf/PdfResult";
import { MostreResult } from "../renderers/cm/mostre/MostreResult";
import { EventiResult } from "../renderers/cm/eventi/EventiResult";
import { PetizioniResult } from "../renderers/cm/petizioni/PetizioniResult";
import { ProcessiResult } from "../renderers/cm/processi/ProcessiResult";
import { OpendataResult } from "../renderers/cm/opendata/OpendataResult";
import { CalendarResult } from "../renderers/openk9/calendar/CalendarResult";
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
        if (result.source.documentTypes.includes("opendata")) {
          return <OpendataResult result={result} />;
        }
        if (result.source.documentTypes.includes("notizie")) {
          return <NotizieResult result={result} />;
        }
        if (result.source.documentTypes.includes("pubblicazioni")) {
          return <PubblicazioniResult result={result} />;
        }
        if (result.source.documentTypes.includes("mostre")) {
          return <MostreResult result={result} />;
        }
        if (result.source.documentTypes.includes("eventi")) {
          return <EventiResult result={result} />;
        }
        if (result.source.documentTypes.includes("petizioni")) {
          return <PetizioniResult result={result} />;
        }
        if (result.source.documentTypes.includes("processi")) {
          return <ProcessiResult result={result} />;
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
        if (result.source.documentTypes.includes("user")) {
          return <UserResult result={result} />;
        }
        if (result.source.documentTypes.includes("calendar")) {
          return <CalendarResult result={result} />;
        }
        return (
          <pre
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
