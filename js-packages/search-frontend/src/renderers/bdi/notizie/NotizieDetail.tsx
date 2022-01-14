import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../../../renderer-components/HighlightedText";
import { NotizieResultItem } from "./NotizieItem";

type NotizieDetailProps = {
  result: NotizieResultItem;
};
export function NotizieDetail({ result }: NotizieDetailProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-row-gap: 8px;
        grid-auto-flow: row;
      `}
    >
      <img
        src={result.source.notizie?.imgUrl}
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
        <strong>Category</strong> : {result.source.notizie?.category}
      </div>
      <div>
        <strong>Topic</strong> : {result.source.notizie?.topic}
      </div>
      <div>
        <strong>Pubblication Date : </strong>
        {result.source.notizie?.pubDate}
      </div>
      <div>
        <strong>Linked urls: </strong>
        <ul>
          {result.source.notizie?.linkedUrls.slice(0, 3).map((url) => {
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
