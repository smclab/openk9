import { faFileAlt } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../../../renderer-components/HighlightedText";
import { DocumentResultItem } from "./DocumentItem";

type DocumentDetailProps = {
  result: DocumentResultItem;
};
export function DocumentDetail({ result }: DocumentDetailProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-row-gap: 8px;
        grid-auto-flow: row;
      `}
    >
      <FontAwesomeIcon icon={faFileAlt} />
      <div
        css={css`
          font-size: 1.5em;
          font-weight: 500;
        `}
      >
        {result.highlight["document.title"] ? (
          <HighlightedText text={result.highlight["document.title"][0]} />
        ) : (
          result.source.document?.title
        )}
      </div>
      <div
        css={css`
          font-size: 0.8em;
        `}
      >
        {result.highlight["document.url"] ? (
          <HighlightedText text={result.highlight["document.url"][0]} />
        ) : (
          result.source.document?.url
        )}
      </div>
      <div>
        {result.highlight["document.content"] ? (
          result.highlight["document.content"].map((text, index) => (
            <div key={index}>
              <HighlightedText text={text} />
            </div>
          ))
        ) : (
          <div>{result.source.document?.content}</div>
        )}
      </div>
    </div>
  );
}
