import { faUser } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../components/HighlightedText";
import { ResultDTO } from "../utils/remote-data";

type UserDetailProps = {
  result: ResultDTO;
};
export function UserDetail({ result }: UserDetailProps) {
  return (
    <div
      css={css`
        display: grid;
        grid-row-gap: 8px;
        grid-auto-flow: row;
      `}
    >
      <FontAwesomeIcon icon={faUser} />
      <div
        css={css`
          font-size: 1.5em;
          font-weight: 500;
        `}
      >
        {result.highlight["user.fullName"] ? (
          <HighlightedText text={result.highlight["user.fullName"][0]} />
        ) : (
          result.source.user?.fullName
        )}
      </div>
      <div>
        <strong>Screen Name</strong> : {result.source.user?.screenName}
      </div>
      <div>
        <strong>Job Title</strong> : {result.source.user?.jobTitle}
      </div>
    </div>
  );
}
