import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../../components/HighlightedText";
import { EventiResultItem } from "./EventiItem";

type EventiDetailProps = {
  result: EventiResultItem;
};
export function EventiDetail({ result }: EventiDetailProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-row-gap: 8px;
        grid-auto-flow: row;
      `}
    >
      <img
        src={result.source.eventi?.imgUrl}
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
      {result.source.eventi?.date && (
        <div>
          <strong>Date</strong> : {result.source.eventi.date}
        </div>
      )}
      {result.source.eventi?.startDate && (
        <div>
          <strong>Start date</strong> : {result.source.eventi.startDate}
        </div>
      )}
      {result.source.eventi?.endDate && (
        <div>
          <strong>End date</strong> : {result.source.eventi.endDate}
        </div>
      )}
      {result.source.eventi?.category && (
        <div>
          <strong>Category</strong> : {result.source.eventi.category}
        </div>
      )}
      <div>
        <strong>Location</strong> : {result.source.eventi?.location}
      </div>
    </div>
  );
}
