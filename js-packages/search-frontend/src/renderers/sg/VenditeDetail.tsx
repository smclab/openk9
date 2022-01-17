import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../../renderer-components/HighlightedText";
import { VenditeResultItem } from "./VenditeItem";

type VenditeDetailProps = {
  result: VenditeResultItem;
};
export function VenditeDetail({ result }: VenditeDetailProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-row-gap: 8px;
        grid-auto-flow: row;
      `}
    >
      <img src={result.source.web?.favicon} alt="" />
      <div
        css={css`
          font-size: 1.5em;
          font-weight: 500;
        `}
      >
        {result.highlight["web.title"] ? (
          <HighlightedText text={result.highlight["web.title"][0]} />
        ) : (
          result.source.web?.title
        )}
      </div>
      <div
        css={css`
          font-size: 0.8em;
        `}
      >
        {result.highlight["web.url"] ? (
          <HighlightedText text={result.highlight["web.url"][0]} />
        ) : (
          result.source.web?.url
        )}
      </div>
      <div>
        <strong>Descrizione</strong> : {result.source.vendite?.descrizione}
      </div>
      <div>
        <strong>Categoria</strong> : {result.source.vendite?.category}
      </div>
      <div>
        <strong>Tipologia</strong> : {result.source.vendite?.tipologia}
      </div>
      <div>
        <strong>Modalità</strong> : {result.source.vendite?.modalita}
      </div>
      <div>
        <strong>Classificazione</strong> : {result.source.vendite?.classificazione}
      </div>
      <div>
        <strong>Tipologia</strong> : {result.source.vendite?.tipologia}
      </div>
      <div>
        <strong>Destinazione</strong> : {result.source.vendite?.destinazione}
      </div>
      <div>
        <strong>Regione</strong> : {result.source.vendite?.regione}
      </div>
      <div>
        <strong>Provincia</strong> : {result.source.vendite?.provincia}
      </div>
      <div>
        <strong>Comune</strong> : {result.source.vendite?.comune}
      </div>
      <div>
        <strong>Indirizzo</strong> : {result.source.vendite?.indirizzo}
      </div>
      <div>
        <strong>Provincia</strong> : {result.source.vendite?.foglio}
      </div>
      <div>
        <strong>Comune</strong> : {result.source.vendite?.mappale}
      </div>
      <div>
        <strong>Indirizzo</strong> : {result.source.vendite?.subalterno}
      </div>
      <div>
        <strong>Data di scadenza</strong> : {result.source.vendite?.scadenzaOfferte}
      </div>
      <div>
        <strong>Data esame offerte</strong> : {result.source.vendite?.esameOfferte}
      </div>
      <div>
        <strong>Prezzo base</strong> : {result.source.vendite?.prezzoBase}
      </div>
      <div>
        <strong>Prezzo di aggiudicazione</strong> : {result.source.vendite?.prezzoDiAggiudicazione}
      </div>
      <div>
        <strong>Referente</strong> : {result.source.vendite?.referente}
      </div>
      <div>
        <strong>Email</strong> : {result.source.vendite?.email}
      </div>
      <div>
        <strong>Documenti di vendite: </strong>
        <ul>
          {result.source.vendite?.documentiVendite?.map((url) => {
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
      </div>
    </div>
  );
}
