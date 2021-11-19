import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../components/HighlightedText";
import { truncatedLineStyle } from "../utils/truncatedLineStyle";
import { ResultDTO } from "../utils/remote-data";
import { Badge } from "../components/Badge";

type PubblicazioniResultProps = { result: ResultDTO };
export function PubblicazioniResult({ result }: PubblicazioniResultProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-template-columns: 30px auto;
        grid-template-rows: auto auto auto auto;
        padding: 8px 16px;
        grid-column-gap: 16px;
        grid-row-gap: 8px;
      `}
    >
      <div
        css={css`
          grid-column: 1;
          grid-row: 1;
          align-self: center;
          width: 30px;
          height: 30px;
          display: flex;
          align-items: center;
          justify-content: center;
        `}
      >
        <img
          src={result.source.web?.favicon}
          alt=""
          css={css`
            max-height: 30px;
            max-width: 30px;
          `}
        />
      </div>
      <div
        css={css`
          grid-column: 2;
          grid-row: 1;
          font-size: 1.5em;
          font-weight: 500;
          ${truncatedLineStyle}
        `}
      >
        {result.highlight["web.title"] ? (
          <HighlightedText text={result.highlight["web.title"][0]} />
        ) : (
          result.source.web?.title
        )}
      </div>
      <a
        href={result.source.web?.url}
        css={css`
          grid-column: 2;
          grid-row: 2;
          font-size: 0.8em;
          ${truncatedLineStyle}
        `}
      >
        {result.highlight["web.url"] ? (
          <HighlightedText text={result.highlight["web.url"][0]} />
        ) : (
          result.source.web?.url
        )}
      </a>
      <div
        css={css`
          grid-column: 2;
          grid-row: 3;
          display: flex;
        `}
      >
        <Badge>{result.source.pubblicazioni?.pubDate}</Badge>
        <Badge>{result.source.pubblicazioni?.topic}</Badge>
      </div>
      <div
        css={css`
          grid-column: 2;
          grid-row: 4;
          ${result.highlight["web.content"] ? truncatedLineStyle : null};
        `}
      >
        {result.highlight["web.content"] ? (
          result.highlight["web.content"].map((text, index) => (
            <div key={index} css={truncatedLineStyle}>
              <HighlightedText text={text} />
            </div>
          ))
        ) : (
          <div
            css={css`
              max-height: calc(21px * 5);
              overflow-y: hidden;
            `}
          >
            {result.source.web?.content}
          </div>
        )}
      </div>
    </div>
  );
}
