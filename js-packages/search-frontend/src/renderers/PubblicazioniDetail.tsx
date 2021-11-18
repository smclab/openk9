import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../components/HighlightedText";
import { ResultDTO } from "../utils/remote-data";

type PubblicazioniDetailProps = {
  result: ResultDTO;
};
export function PubblicazioniDetail({ result }: PubblicazioniDetailProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-row-gap: 8px;
        grid-auto-flow: row;
      `}
    >
      <img
        src={result.source.pubblicazioni?.imgUrl}
        alt=""
        css={css`
          max-width: calc(50vw - 32px);
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
        {result.highlight["web.url"] ? (
          <HighlightedText text={result.highlight["web.url"][0]} />
        ) : (
          result.source.web?.url
        )}
      </div>
      <div>
        <strong>Category</strong> : {result.source.pubblicazioni?.category}
      </div>
      <div>
        <strong>Topic</strong> : {result.source.pubblicazioni?.topic}
      </div>
      <div>
        <strong>Authors</strong> : {result.source.pubblicazioni?.authors}
      </div>
      <div>
        <strong>Pubblication Date : </strong>
        {result.source.pubblicazioni?.pubDate}
      </div>
      <div>
        {result.highlight["web.content"] ? (
          result.highlight["web.content"].map((text, index) => (
            <div key={index}>
              <HighlightedText text={text} />
            </div>
          ))
        ) : (
          <div>{result.source.web?.content}</div>
        )}
      </div>
    </div>
  );
}
