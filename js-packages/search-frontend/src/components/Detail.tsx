import React from "react";
import { css } from "styled-components/macro";
import { WebDetail } from "../renderers/openk9/web/WebDetail";
import { GenericResultItem } from "@openk9/rest-api";
import { DocumentDetail } from "../renderers/openk9/document/DocumentDetail";
import { EmailDetail } from "../renderers/openk9/email/EmailDetail";
import { UserDetail } from "../renderers/openk9/user/UserDetail";
import { NotizieDetail } from "../renderers/bdi/notizie/NotizieDetail";
import { PubblicazioniDetail } from "../renderers/bdi/pubblicazioni/PubblicazioniDetail";
import { PdfDetail } from "../renderers/openk9/pdf/PdfDetail";
import { MostreDetail } from "../renderers/cm/mostre/MostreDetail";
import { EventiDetail } from "../renderers/cm/eventi/EventiDetail";
import { PetizioniDetail } from "../renderers/cm/petizioni/PetizioniDetail";
import { ProcessiDetail } from "../renderers/cm/processi/ProcessiDetail";
import { WemiDetail } from "../renderers/cm/wemi/WemiDetail";
import { EntrateDetail } from "../renderers/sg/entrate/EntrateDetail";
import { AssistenzaDetail } from "../renderers/sg/assistenza/AssistenzaDetail";
import { GaraDetail } from "../renderers/sg/gara/GaraDetail";
import { OpendataDetail } from "../renderers/cm/opendata/OpendataDetail";

type DetailProps<E> = {
  result: GenericResultItem<E>;
};
function Detail<E>(props: DetailProps<E>) {
  const result = props.result as any;
  return (
    <div
      css={css`
        padding: 8px 16px;
      `}
    >
      {(() => {
        if (result.source.documentTypes.includes("gara")) {
          return <GaraDetail result={result} />;
        }
        if (result.source.documentTypes.includes("opendata")) {
          return <OpendataDetail result={result} />;
        }
        if (result.source.documentTypes.includes("notizie")) {
          return <NotizieDetail result={result} />;
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
        if (result.source.documentTypes.includes("wemi")) {
          return <WemiDetail result={result} />;
        }
        if (result.source.documentTypes.includes("entrate")) {
          return <EntrateDetail result={result} />;
        }
        if (result.source.documentTypes.includes("entratel")) {
          return <AssistenzaDetail result={result} />;
        }
        if (result.source.documentTypes.includes("fisco")) {
          return <AssistenzaDetail result={result} />;
        }
        if (result.source.documentTypes.includes("pdf")) {
          return <PdfDetail result={result} />;
        }
        if (result.source.documentTypes.includes("excel")) {
          return <PdfDetail result={result} />;
        }
        if (result.source.documentTypes.includes("document")) {
          return <DocumentDetail result={result} />;
        }
        if (result.source.documentTypes.includes("web")) {
          return <WebDetail result={result} />;
        }
        if (result.source.documentTypes.includes("email")) {
          return <EmailDetail result={result} />;
        }
        if (result.source.documentTypes.includes("user")) {
          return <UserDetail result={result} />;
        }
        return <pre css={css``}>{JSON.stringify(result, null, 2)}</pre>;
      })()}
    </div>
  );
}
export const DetailMemo = React.memo(Detail);
