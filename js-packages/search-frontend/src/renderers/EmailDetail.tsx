import { faEnvelope } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../renderer-components/HighlightedText";
import { myTheme } from "../utils/myTheme";
import { EmailResultItem } from "./EmailItem";

type EmailDetailProps = {
  result: EmailResultItem;
};
export function EmailDetail({ result }: EmailDetailProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-row-gap: 8px;
        grid-auto-flow: row;
      `}
    >
      <FontAwesomeIcon icon={faEnvelope} />
      <div
        css={css`
          font-size: 1.5em;
          font-weight: 500;
        `}
      >
        {result.highlight["email.subject"] ? (
          <HighlightedText text={result.highlight["email.subject"][0]} />
        ) : (
          result.source.email?.subject
        )}
      </div>
      <div>
        <strong>From</strong> : {result.source.email?.from}
      </div>
      <div>
        <strong>To</strong> : {result.source.email?.to}
      </div>
      {result.source.email?.cc && (
        <div>
          <strong>Cc</strong> : {result.source.email?.cc}
        </div>
      )}
      <div>
        {result.highlight["email.body"] &&
          result.highlight["email.body"].map((text, index) => (
            <div key={index}>
              <HighlightedText text={text} />
            </div>
          ))}
      </div>
      {!result.highlight["email.body"] && !result.source.email.htmlBody && (
        <div>{result.source.email?.body}</div>
      )}
      {result.source.email.htmlBody && (
        <iframe
          title={result.source.contentId}
          srcDoc={result.source.email.htmlBody}
          css={css`
            width: 100%;
            height: 50vh;
            border: 1px solid ${myTheme.searchBarBorderColor};
          `}
        />
      )}
    </div>
  );
}
