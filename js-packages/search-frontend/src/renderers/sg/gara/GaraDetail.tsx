import React from "react";
import { css } from "styled-components/macro";
import { DetailAttribute } from "../../../renderer-components/DetailAttribute";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailFavicon } from "../../../renderer-components/DetailFavicon";
import { DetailLink } from "../../../renderer-components/DetailLink";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { GaraResultItem } from "./GaraItem";

type GaraDetailProps = {
  result: GaraResultItem;
};
export function GaraDetail({ result }: GaraDetailProps) {
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
        {result.source.gare.descrizione}
      </DetailAttribute>
      <DetailAttribute label="Oggetto della gara">
        {result.source.gare.oggettoGara}
      </DetailAttribute>
      <DetailAttribute label="Stato gara">
        {result.source.gare.status}
      </DetailAttribute>
      <DetailAttribute label="Tipologia">
        {result.source.gare.tipologia}
      </DetailAttribute>
      <DetailAttribute label="Regione">
        {result.source.gare.regione}
      </DetailAttribute>
      <DetailAttribute label="Provincia">
        {result.source.gare.provincia}
      </DetailAttribute>
      <DetailAttribute label="Comune">
        {result.source.gare.comune}
      </DetailAttribute>
      <DetailAttribute label="Stazione">
        {result.source.gare.stazione}
      </DetailAttribute>
      <DetailAttribute label="Data di pubblicazione">
        {result.source.gare.datapubblicazione}
      </DetailAttribute>
      <DetailAttribute label="Data di scadenza">
        {result.source.gare.datascadenza}
      </DetailAttribute>
      <DetailAttribute label="Data esito">
        {result.source.gare.dataEsito}
      </DetailAttribute>
      <DetailAttribute label="Importo">
        {result.source.gare.importo}
      </DetailAttribute>
      <DetailAttribute label="Criterio">
        {result.source.gare.criterio}
      </DetailAttribute>
      <DetailAttribute label="Nominativo">
        {result.source.gare.nominativo}
      </DetailAttribute>
      <DetailAttribute label="Email">
        {result.source.gare.email}
      </DetailAttribute>
      <DetailAttribute label="Documenti di gara">
        <ul>
          {result.source.gare.documentiGara?.map((url) => {
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
        </ul>
      </DetailAttribute>
      <DetailAttribute label="Documenti di esito">
        <ul>
          {result.source.gare.documentiEsito?.map((url) => {
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
        </ul>
      </DetailAttribute>
    </DetailContainer>
  );
}
