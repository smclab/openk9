import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../../components/HighlightedText";
import { ResultDTO } from "../../utils/remote-data";

type PetizioniDetailProps = {
  result: ResultDTO;
};
export function PetizioniDetail({ result }: PetizioniDetailProps) {
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
        <a href={result.source.web?.url}>
          {result.highlight["web.url"] ? (
            <HighlightedText text={result.highlight["web.url"][0]} />
          ) : (
            result.source.web?.url
          )}
        </a>
      </div>
      {result.source.petizioni?.status && (
        <div>
          <strong>Status</strong> : {result.source.petizioni.status}
        </div>
      )}
      {result.source.petizioni?.pubDate && (
        <div>
          <strong>Pubblication date</strong> : {result.source.petizioni.pubDate}
        </div>
      )}
    </div>
  );
}
