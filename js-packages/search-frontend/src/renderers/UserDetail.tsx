import { faUser } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import { css } from "styled-components/macro";
import { HighlightedText } from "../renderer-components/HighlightedText";
import { UserResultItem } from "./UserItem";

type UserDetailProps = {
  result: UserResultItem;
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
      {result.source.user?.jobClass && (
        <div>
          <strong>Job Class</strong> : {result.source.user?.jobClass}
        </div>
      )}
      <div>
        <strong>Email</strong> : {result.source.user?.emailAddress}
      </div>
      <div>
        <strong>Birthday</strong> : {result.source.user?.birthday}
      </div>
      <div>
        <strong>Job Title</strong> : {result.source.user?.jobTitle}
      </div>
      <div>
        <strong>Phone Number</strong> : {result.source.user?.phoneNumber}
      </div>
      <div>
        <strong>Facebook</strong> : {result.source.user?.facebookSn}
      </div>
      {result.source.user?.twitterSn && (
        <div>
          <strong>Twitter</strong> : {result.source.user?.twitterSn}
        </div>
      )}
      {result.source.user?.skypeSn && (
        <div>
          <strong>Skype</strong> : {result.source.user?.skypeSn}
        </div>
      )}
      <div>
        <strong>User Id</strong> : {result.source.user?.userId}
      </div>
    </div>
  );
}
