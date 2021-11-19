import { faFilePdf } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../components/HighlightedText";
import { ResultDTO } from "../utils/remote-data";

type PdfDetailProps = {
  result: ResultDTO;
};
export function PdfDetail({ result }: PdfDetailProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-row-gap: 8px;
        grid-auto-flow: row;
      `}
    >
      <FontAwesomeIcon icon={faFilePdf} />
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
      {result.source.file?.lastModifiedDate && (
        <div>
          <strong>Last modified</strong> :{" "}
          {new Date(result.source.file?.lastModifiedDate).toLocaleString()}
        </div>
      )}
      <div>
        {result.source.resources.binaries.map((binary) => {
          return (
            <iframe
              key={binary.id}
              title={binary.id}
              src={`/api/searcher/resources/${result.source.datasourceId}/${result.source.id}/${binary.id}`}
              css={css`
                width: calc(50vw - 33px);
                height: 65vh;
                border: none;
                border-radius: 4px;
              `}
            />
          );
        })}
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
