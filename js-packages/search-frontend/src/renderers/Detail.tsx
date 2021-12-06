import React from "react";
import { css } from "styled-components/macro";
import { WebDetail } from "./WebDetail";
import { ResultDTO } from "../utils/remote-data";
import { DocumentDetail } from "./DocumentDetail";
import { NotizieDetail } from "./bdi/NotizieDetail";
import { PubblicazioniDetail } from "./bdi/PubblicazioniDetail";
import { PdfDetail } from "./PdfDetail";
import { MostreDetail } from "./cm/MostreDetail";
import { EventiDetail } from "./cm/EventiDetail";

type DetailProps = {
  result: ResultDTO;
};
function Detail({ result }: DetailProps) {
  return (
    <div
      css={css`
        padding: 8px 16px;
      `}
    >
      {(() => {
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
  );
}
export const DetailMemo = React.memo(Detail);
