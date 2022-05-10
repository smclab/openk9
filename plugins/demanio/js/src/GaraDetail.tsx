import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { GaraResultItem } from "./GaraItem";
import { rendererComponents } from "@openk9/search-frontend";

const {
  DetailAttribute,
  DetailContainer,
  DetailFavicon,
  DetailLink,
  DetailTextContent,
  DetailTitle,
  DetailHeaderImage,
  HighlightableText,
} = rendererComponents;

type GaraDetailProps = {
  result: GenericResultItem<GaraResultItem>;
};
export function GaraDetail({ result }: GaraDetailProps) {
  return (
    <DetailContainer>
      {result.source.gare.imgUrl ? (
        <DetailHeaderImage src={result.source.gare.imgUrl} />
      ) : (
        <DetailFavicon src={result.source.web.favicon} />
      )}
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
                  style={{ wordBreak: "break-all" }}
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
                  style={{ wordBreak: "break-all" }}
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
