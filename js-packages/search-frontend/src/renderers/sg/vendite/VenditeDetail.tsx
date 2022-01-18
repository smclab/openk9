import React from "react";
import { css } from "styled-components/macro";
import { DetailAttribute } from "../../../renderer-components/DetailAttribute";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailFavicon } from "../../../renderer-components/DetailFavicon";
import { DetailLink } from "../../../renderer-components/DetailLink";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { VenditeResultItem } from "./VenditeItem";

type VenditeDetailProps = {
  result: VenditeResultItem;
};
export function VenditeDetail({ result }: VenditeDetailProps) {
  return (
    <DetailContainer>
      <DetailFavicon src={result.source.web.favicon} />
      <DetailTitle>
        <HighlightableText result={result} path="web.title" />
      </DetailTitle>
      <DetailLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </DetailLink>
      <DetailAttribute label="Descrizione">
        {result.source.vendite.descrizione}
      </DetailAttribute>
      <DetailAttribute label="Categoria">
        {result.source.vendite.category}
      </DetailAttribute>
      <DetailAttribute label="Tipologia">
        {result.source.vendite.tipologia}
      </DetailAttribute>
      <DetailAttribute label="Modalità">
        {result.source.vendite.modalita}
      </DetailAttribute>
      <DetailAttribute label="Classificazione">
        {result.source.vendite.classificazione}
      </DetailAttribute>
      <DetailAttribute label="Tipologia">
        {result.source.vendite.tipologia}
      </DetailAttribute>
      <DetailAttribute label="Destinazione">
        {result.source.vendite.destinazione}
      </DetailAttribute>
      <DetailAttribute label="Regione">
        {result.source.vendite.regione}
      </DetailAttribute>
      <DetailAttribute label="Provincia">
        {result.source.vendite.provincia}
      </DetailAttribute>
      <DetailAttribute label="Comune">
        {result.source.vendite.comune}
      </DetailAttribute>
      <DetailAttribute label="Indirizzo">
        {result.source.vendite.indirizzo}
      </DetailAttribute>
      <DetailAttribute label="Provincia">
        {result.source.vendite.foglio}
      </DetailAttribute>
      <DetailAttribute label="Comune">
        {result.source.vendite.mappale}
      </DetailAttribute>
      <DetailAttribute label="Indirizzo">
        {result.source.vendite.subalterno}
      </DetailAttribute>
      <DetailAttribute label="Data di scadenza">
        {result.source.vendite.scadenzaOfferte}
      </DetailAttribute>
      <DetailAttribute label="Data esame offerte">
        {result.source.vendite.esameOfferte}
      </DetailAttribute>
      <DetailAttribute label="Prezzo base">
        {result.source.vendite.prezzoBase}
      </DetailAttribute>
      <DetailAttribute label="Prezzo di aggiudicazione">
        {result.source.vendite.prezzoDiAggiudicazione}
      </DetailAttribute>
      <DetailAttribute label="Referente">
        {result.source.vendite.referente}
      </DetailAttribute>
      <DetailAttribute label="Email">
        {result.source.vendite.email}
      </DetailAttribute>
      <DetailAttribute label="Documenti di vendite">
        <ul>
          {result.source.vendite.documentiVendite?.map((url) => {
            return (
              <li key={url}>
                <a
                  href={url}
                  target="_blank"
                  rel="noreferrer"
                  css={css`
                    word-break: break-all;
                  `}
                >
                  {url}
                </a>
              </li>
            );
          })}
        </ul>{" "}
      </DetailAttribute>
    </DetailContainer>
  );
}
