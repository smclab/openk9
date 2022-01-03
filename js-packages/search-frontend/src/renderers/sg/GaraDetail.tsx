import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../../components/HighlightedText";
import { ResultDTO } from "../../utils/remote-data";

type GaraDetailProps = {
  result: ResultDTO;
};
export function GaraDetail({ result }: GaraDetailProps) {
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
        <strong>Descrizione</strong> : {result.source.gara?.descrizione}
      </div>
      <div>
        <strong>Oggetto della gara</strong> : {result.source.gara?.oggettoGara}
      </div>
      <div>
        <strong>Stato gara</strong> : {result.source.gara?.status}
      </div>
      <div>
        <strong>Tipologia</strong> : {result.source.gara?.tipologia}
      </div>
      <div>
        <strong>Regione</strong> : {result.source.gara?.regione}
      </div>
      <div>
        <strong>Provincia</strong> : {result.source.gara?.provincia}
      </div>
      <div>
        <strong>Comune</strong> : {result.source.gara?.comune}
      </div>
      <div>
        <strong>stazione</strong> : {result.source.gara?.stazione}
      </div>
      <div>
        <strong>Data di pubblicazione</strong> : {result.source.gara?.datapubblicazione}
      </div>
      <div>
        <strong>Data di scadenza</strong> : {result.source.gara?.datascadenza}
      </div>
      <div>
        <strong>Data esito</strong> : {result.source.gara?.dataEsito}
      </div>
      <div>
        <strong>Importo</strong> : {result.source.gara?.importo}
      </div>
      <div>
        <strong>Criterio</strong> : {result.source.gara?.criterio}
      </div>
      <div>
        <strong>Nominativo</strong> : {result.source.gara?.nominativo}
      </div>
      <div>
        <strong>Email</strong> : {result.source.gara?.email}
      </div>
      <div>
        <strong>Documenti di gara: </strong>
        <ul>
          {result.source.gara?.documentiGara?.map((url) => {
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
      <div>
        <strong>Documenti di esito: </strong>
        <ul>
          {result.source.gara?.documentiEsito?.map((url) => {
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
