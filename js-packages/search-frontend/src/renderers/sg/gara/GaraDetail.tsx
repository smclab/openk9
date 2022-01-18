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
        {result.source.gara.descrizione}
      </DetailAttribute>
      <DetailAttribute label="Oggetto della gara">
        {result.source.gara.oggettoGara}
      </DetailAttribute>
      <DetailAttribute label="Stato gara">
        {result.source.gara.status}
      </DetailAttribute>
      <DetailAttribute label="Tipologia">
        {result.source.gara.tipologia}
      </DetailAttribute>
      <DetailAttribute label="Regione">
        {result.source.gara.regione}
      </DetailAttribute>
      <DetailAttribute label="Provincia">
        {result.source.gara.provincia}
      </DetailAttribute>
      <DetailAttribute label="Comune">
        {result.source.gara.comune}
      </DetailAttribute>
      <DetailAttribute label="Stazione">
        {result.source.gara.stazione}
      </DetailAttribute>
      <DetailAttribute label="Data di pubblicazione">
        {result.source.gara.datapubblicazione}
      </DetailAttribute>
      <DetailAttribute label="Data di scadenza">
        {result.source.gara.datascadenza}
      </DetailAttribute>
      <DetailAttribute label="Data esito">
        {result.source.gara.dataEsito}
      </DetailAttribute>
      <DetailAttribute label="Importo">
        {result.source.gara.importo}
      </DetailAttribute>
      <DetailAttribute label="Criterio">
        {result.source.gara.criterio}
      </DetailAttribute>
      <DetailAttribute label="Nominativo">
        {result.source.gara.nominativo}
      </DetailAttribute>
      <DetailAttribute label="Email">
        {result.source.gara.email}
      </DetailAttribute>
      <DetailAttribute label="Documenti di gara">
        <ul>
          {result.source.gara.documentiGara.map((url) => {
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
          {result.source.gara.documentiEsito.map((url) => {
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
