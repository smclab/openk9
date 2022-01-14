import React from "react";
import { css } from "styled-components/macro";
import { truncatedLineStyle } from "../../../renderer-components/truncatedLineStyle";
import { HighlightedText } from "../../../renderer-components/HighlightedText";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFileAlt } from "@fortawesome/free-solid-svg-icons";
import { DocumentResultItem } from "./DocumentItem";

type DocumentResultProps = { result: DocumentResultItem };
export function DocumentResult({ result }: DocumentResultProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-template-columns: 30px auto;
        grid-template-rows: auto auto auto;
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
        <FontAwesomeIcon icon={faFileAlt} />
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
        {result.highlight["document.title"] ? (
          <HighlightedText text={result.highlight["document.title"][0]} />
        ) : (
          result.source.document?.title
        )}
      </div>
      <a
        href={result.source.document?.url}
        target="_blank"
        rel="noreferrer"
        css={css`
          grid-column: 2;
          grid-row: 2;
          font-size: 0.8em;
          ${truncatedLineStyle}
        `}
      >
        {result.highlight["document.url"] ? (
          <HighlightedText text={result.highlight["document.url"][0]} />
        ) : (
          result.source.document?.url
        )}
      </a>
      <div
        css={css`
          grid-column: 2;
          grid-row: 3;
          ${result.highlight["document.content"] ? truncatedLineStyle : null};
        `}
      >
        {result.highlight["document.content"] ? (
          result.highlight["document.content"].map((text, index) => (
            <div key={index} css={truncatedLineStyle}>
              <HighlightedText text={text} />
            </div>
          ))
        ) : (
          <div
            css={css`
              max-height: calc(21px * 5);
              overflow-y: hidden;
              word-wrap: break-word;
              word-break: break-word;
            `}
          >
            {result.source.document?.content}
          </div>
        )}
      </div>
    </div>
  );
}
