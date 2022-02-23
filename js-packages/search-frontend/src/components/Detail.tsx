import React from "react";
import { css } from "styled-components/macro";
import { WebDetail } from "../renderers/openk9/web/WebDetail";
import { GenericResultItem, SidebarRendererProps } from "@openk9/rest-api";
import { DocumentDetail } from "../renderers/openk9/document/DocumentDetail";
import { UserDetail } from "../renderers/openk9/user/UserDetail";
import { PubblicazioniDetail } from "../renderers/bdi/pubblicazioni/PubblicazioniDetail";
import { PdfDetail } from "../renderers/openk9/pdf/PdfDetail";
import { MostreDetail } from "../renderers/cm/mostre/MostreDetail";
import { EventiDetail } from "../renderers/cm/eventi/EventiDetail";
import { PetizioniDetail } from "../renderers/cm/petizioni/PetizioniDetail";
import { ProcessiDetail } from "../renderers/cm/processi/ProcessiDetail";
import { EntrateDetail } from "../renderers/sg/entrate/EntrateDetail";
import { AssistenzaDetail } from "../renderers/sg/assistenza/AssistenzaDetail";
import { GaraDetail } from "../renderers/sg/gara/GaraDetail";
import { VenditeDetail } from "../renderers/sg/vendite/VenditeDetail";
import { CalendarDetail } from "../renderers/openk9/calendar/CalendarDetail";
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
          if (result.source.documentTypes.includes("pubblicazioni")) {
            return <PubblicazioniDetail result={result} />;
          }
          if (result.source.documentTypes.includes("mostre")) {
            return <MostreDetail result={result} />;
          }
          if (result.source.documentTypes.includes("eventi")) {
            return <EventiDetail result={result} />;
          }
          if (result.source.documentTypes.includes("petizioni")) {
            return <PetizioniDetail result={result} />;
          }
          if (result.source.documentTypes.includes("processi")) {
            return <ProcessiDetail result={result} />;
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
          if (result.source.documentTypes.includes("user")) {
            return <UserDetail result={result} />;
          }
          if (result.source.documentTypes.includes("calendar")) {
            return <CalendarDetail result={result} />;
          }
          return <pre css={css``}>{JSON.stringify(result, null, 2)}</pre>;
        })()}
      </div>
    </div>
  );
}
export const DetailMemo = React.memo(Detail);
