import React from "react";
import { css } from "styled-components/macro";
import { ResultDTO } from "../utils/remote-data";
import { truncatedLineStyle } from "../utils/truncatedLineStyle";
import { HighlightedText } from "../components/HighlightedText";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faUser } from "@fortawesome/free-solid-svg-icons";

type UserResultProps = { result: ResultDTO };
export function UserResult({ result }: UserResultProps) {
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
        <FontAwesomeIcon icon={faUser} />
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
        {result.highlight["email.fullName"] ? (
          <HighlightedText text={result.highlight["user.fullName"][0]} />
        ) : (
          result.source.user?.fullName
        )}
      </div>
      <div
        css={css`
          grid-column: 1;
          grid-row: 1;
          font-size: 1em;
        `}
        >
        {result.highlight["user.jobTitle"] ? (
          <HighlightedText text={result.highlight["user.jobTitle"][0]} />
        ) : (
          result.source.user?.jobTitle
        )}
      </div>
      <div
        css={css`
          grid-column: 1;
          grid-row: 1;
          font-size: 1em;
        `}
        >
        {result.highlight["user.emailAddress"] ? (
          <HighlightedText text={result.highlight["user.emailAddress"][0]} />
        ) : (
          result.source.user?.emailAddress
        )}
      </div>
      <div
        css={css`
          grid-column: 1;
          grid-row: 1;
          font-size: 1em;
        `}
        >
        {result.highlight["user.employeeNumber"] ? (
          <HighlightedText text={result.highlight["user.phoneNumber"][0]} />
        ) : (
          result.source.user?.employeeNumber
        )}
      </div>
    </div>
  );
}
