import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../../renderer-components/HighlightedText";
import { MostreResultItem } from "./MostreItem";

type MostreDetailProps = {
  result: MostreResultItem;
};
export function MostreDetail({ result }: MostreDetailProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-row-gap: 8px;
        grid-auto-flow: row;
      `}
    >
      <img
        src={result.source.mostre?.imgUrl}
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
      <div>
        <strong>Start date</strong> : {result.source.mostre?.startDate}
      </div>
      <div>
        <strong>End date</strong> : {result.source.mostre?.endDate}
      </div>
      <div>
        <strong>Location</strong> : {result.source.mostre?.location}
      </div>
    </div>
  );
}
