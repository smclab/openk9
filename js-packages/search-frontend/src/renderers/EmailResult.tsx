import React from "react";
import { css } from "styled-components/macro";
import { truncatedLineStyle } from "../utils/truncatedLineStyle";
import { HighlightedText } from "../renderer-components/HighlightedText";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faEnvelope } from "@fortawesome/free-solid-svg-icons";
import { EmailResultItem } from "./EmailItem";
import { Badge } from "../renderer-components/Badge";

type EmailResultProps = { result: EmailResultItem };
export function EmailResult({ result }: EmailResultProps) {
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
        <FontAwesomeIcon icon={faEnvelope} />
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
        {result.highlight["email.subject"] ? (
          <HighlightedText text={result.highlight["email.subject"][0]} />
        ) : (
          result.source.email?.subject
        )}
      </div>
      <div
        css={css`
          grid-column: 2;
          grid-row: 2;
        `}
      >
        <div>
          <strong>date: </strong>
          {datetimeFormatter.format(result.source.email.date)}
        </div>
        <div>
          <strong>from: </strong>
          {result.source.email.from}
        </div>
        <div>
          <strong>to: </strong>
          {result.source.email.to}
        </div>
      </div>
      <div
        css={css`
          grid-column: 2;
          grid-row: 3;
          ${result.highlight["email.body"] ? truncatedLineStyle : ""};
        `}
      >
        {result.highlight["email.body"] ? (
          result.highlight["email.body"].map((text, index) => (
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
            {result.source.email?.body}
          </div>
        )}
      </div>
    </div>
  );
}

const datetimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: "full",
  timeStyle: "medium",
});
