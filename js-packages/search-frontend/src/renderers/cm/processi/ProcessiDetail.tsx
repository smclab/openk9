import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../../../renderer-components/HighlightedText";
import { ProcessiResultItem } from "./ProcessiItem";

type ProcessiDetailProps = {
  result: ProcessiResultItem;
};
export function ProcessiDetail({ result }: ProcessiDetailProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-row-gap: 8px;
        grid-auto-flow: row;
      `}
    >
      <img
        src={result.source.processi?.imgUrl}
        alt=""
        css={css`
          max-width: 100%;
        `}
      />
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
        <a href={result.source.web?.url}>
          {result.highlight["web.url"] ? (
            <HighlightedText text={result.highlight["web.url"][0]} />
          ) : (
            result.source.web?.url
          )}
        </a>
      </div>
      {result.source.processi?.name && (
        <div>
          <strong>Name</strong> : {result.source.processi.name}
        </div>
      )}
      {result.source.processi?.startDate && (
        <div>
          <strong>Start date</strong> : {result.source.processi.startDate}
        </div>
      )}
      {result.source.processi?.endDate && (
        <div>
          <strong>End date</strong> : {result.source.processi.endDate}
        </div>
      )}
      {result.source.processi?.partecipants && (
        <div>
          <strong>Partecipants</strong> : {result.source.processi.partecipants}
        </div>
      )}
      {result.source.processi?.area && (
        <div>
          <strong>Area</strong> : {result.source.processi.area}
        </div>
      )}
    </div>
  );
}
