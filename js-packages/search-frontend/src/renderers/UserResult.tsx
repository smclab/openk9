import React from "react";
import { css } from "styled-components/macro";
import { truncatedLineStyle } from "../renderer-components/truncatedLineStyle";
import { HighlightedText } from "../renderer-components/HighlightedText";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faUser } from "@fortawesome/free-solid-svg-icons";
import { UserResultItem } from "./UserItem";

type UserResultProps = { result: UserResultItem };
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
        {result.highlight["user.fullName"] ? (
          <HighlightedText text={result.highlight["user.fullName"][0]} />
        ) : (
          result.source.user?.fullName
        )}
      </div>
      <div
        css={css`
          grid-column: 2;
          grid-row: 2;
          font-size: 1em;
        `}
      >
        <strong>Job: </strong>
        {result.highlight["user.jobTitle"] ? (
          <HighlightedText text={result.highlight["user.jobTitle"][0]} />
        ) : (
          result.source.user?.jobTitle
        )}
      </div>
      <div
        css={css`
          grid-row: 3;
          grid-column: 2;
          font-size: 1em;
        `}
      >
        <strong>Email: </strong>
        {result.highlight["user.emailAddress"] ? (
          <HighlightedText text={result.highlight["user.emailAddress"][0]} />
        ) : (
          result.source.user?.emailAddress
        )}
      </div>
      <div
        css={css`
          grid-column: 2;
          grid-row: 4;
          font-size: 1em;
        `}
      >
        <strong>Phone: </strong>
        {result.highlight["user.phoneNumber"] ? (
          <HighlightedText text={result.highlight["user.phoneNumber"][0]} />
        ) : (
          result.source.user?.phoneNumber
        )}
      </div>
    </div>
  );
}
