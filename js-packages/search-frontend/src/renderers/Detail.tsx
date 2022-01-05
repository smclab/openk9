import React from "react";
import { css } from "styled-components/macro";
import { WebDetail } from "./WebDetail";
import { GenericResultItem } from "@openk9/rest-api";
import { DocumentDetail } from "./DocumentDetail";
import { EmailDetail } from "./EmailDetail";
import { UserDetail } from "./UserDetail";
import { NotizieDetail } from "./bdi/NotizieDetail";
import { PubblicazioniDetail } from "./bdi/PubblicazioniDetail";
import { PdfDetail } from "./PdfDetail";
import { MostreDetail } from "./cm/MostreDetail";
import { EventiDetail } from "./cm/EventiDetail";
import { PetizioniDetail } from "./cm/PetizioniDetail";
import { ProcessiDetail } from "./cm/ProcessiDetail";
import { WemiDetail } from "./cm/WemiDetail";
import { EntrateDetail } from "./sg/EntrateDetail";
import { AssistenzaDetail } from "./sg/AssistenzaDetail";
import { GaraDetail } from "./sg/GaraDetail";
import { OpendataDetail } from "./cm/OpendataDetail";

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
