import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../../components/HighlightedText";
import { ResultDTO } from "../../utils/remote-data";

type WemiDetailProps = {
  result: ResultDTO;
};
export function WemiDetail({ result }: WemiDetailProps) {
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
        <strong>Categoria</strong> : {result.source.wemi?.categoria}
      </div>
      <div>
        <strong>Categoria</strong> : {result.source.wemi?.categoria}
      </div>
      <div>
        <strong>Servizio</strong> : {result.source.wemi?.servizio}
      </div>
      <div>
        <strong>Destinatari</strong> :
        <ul>
          {result.source.wemi?.destinatari.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </div>
      <div>
        <strong>Attività</strong> :
        <ul>
          {result.source.wemi?.attività.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </div>
      <div>
        <strong>Prezzi</strong> :
        <ul>
          {result.source.wemi?.prezzi.map((item, index) => {
            return (
              <li key={index}>
                {item.label}: {item.value}
              </li>
            );
          })}
        </ul>
      </div>
      <div>
        <strong>Momento</strong> :
        <ul>
          {result.source.wemi?.momento.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </div>
      <div>
        <strong>Sedi</strong> :
        <ul>
          {result.source.wemi?.sedi.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </div>
      <div>
        <strong>Procedura</strong> : {result.source.wemi?.procedura}
      </div>
    </div>
  );
}
