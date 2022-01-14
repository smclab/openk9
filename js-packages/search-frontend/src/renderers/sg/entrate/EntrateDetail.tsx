import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../../../renderer-components/HighlightedText";
import { EntrateResultItem } from "./EntrateItem";

type EntrateDetailProps = {
  result: EntrateResultItem;
};
export function EntrateDetail({ result }: EntrateDetailProps) {
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
        <strong>Topics</strong> :
        <ul>
          {result.source.topic?.topics?.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </div>
      <div>
        <strong>Linked urls: </strong>
        <ul>
          {result.source.entrate?.linkedUrls.slice(0, 3).map((url) => {
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
