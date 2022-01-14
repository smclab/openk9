import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../../renderer-components/HighlightedText";
import { OpendataResultItem } from "./OpendataItem";

type OpendataDetailProps = {
  result: OpendataResultItem;
};
export function OpendataDetail({ result }: OpendataDetailProps) {
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
        <strong>Data di inizio</strong> : {result.source.opendata?.startDate}
      </div>
      <div>
        <strong>Data di fine</strong> : {result.source.opendata?.endDate}
      </div>
      <div>
        <strong>Ultima data di modifica</strong> :{" "}
        {result.source.opendata?.dataDiModifica}
      </div>
      <div>
        <strong>Titolare</strong> : {result.source.opendata?.titolare}
      </div>
      <div>
        <strong>Copertura geografica</strong> :{" "}
        {result.source.opendata?.coperturaGeografica}
      </div>
      <div>
        <strong>Autore</strong> : {result.source.opendata?.autore}
      </div>
      <div>
        <strong>Temi del dataset</strong> :
        <ul>
          {result.source.opendata?.temiDelDataset?.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </div>
      <div>
        <strong>Tags</strong> :
        <ul>
          {result.source.opendata?.tags?.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </div>
      <div>
        <strong>Summary</strong> : {result.source.opendata?.summary}
      </div>
    </div>
  );
}
