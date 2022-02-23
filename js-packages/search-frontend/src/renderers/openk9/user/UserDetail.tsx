import React from "react";
import { UserResultItem } from "./UserItem";
import { GenericResultItem } from "@openk9/rest-api";
import { faUser } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  DetailAttribute,
  DetailContainer,
  DetailIconContainer,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type UserDetailProps = {
  result: GenericResultItem<UserResultItem>;
};
export function UserDetail({ result }: UserDetailProps) {
  return (
    <DetailContainer>
      <DetailIconContainer>
        <FontAwesomeIcon icon={faUser} />
      </DetailIconContainer>
      <DetailTitle>
        <HighlightableText result={result} path="user.fullName" />
      </DetailTitle>
      <DetailAttribute label="Screen Name">
        <HighlightableText result={result} path="user.screenName" />
      </DetailAttribute>
      <DetailAttribute label="Job Title">
        <HighlightableText result={result} path="user.jobTitle" />
      </DetailAttribute>
      {result.source.user.jobClass && (
        <DetailAttribute label="Job Class">
          <HighlightableText result={result} path="user.jobClass" />
        </DetailAttribute>
      )}
      <DetailAttribute label="Email">
        <HighlightableText result={result} path="user.emailAddress" />
      </DetailAttribute>
      <DetailAttribute label="Birthday">
        <HighlightableText result={result} path="user.birthday" />
      </DetailAttribute>
      <DetailAttribute label="Phone Number">
        <HighlightableText result={result} path="user.phoneNumber" />
      </DetailAttribute>
      {result.source.user.facebookSn && (
        <DetailAttribute label="Facebook">
          <HighlightableText result={result} path="user.facebookSn" />
        </DetailAttribute>
      )}
      {result.source.user.twitterSn && (
        <DetailAttribute label="Twitter">
          <HighlightableText result={result} path="user.twitterSn" />
        </DetailAttribute>
      )}
      {result.source.user.skypeSn && (
        <DetailAttribute label="Skype">
          <HighlightableText result={result} path="user.skypeSn" />
        </DetailAttribute>
      )}
      <DetailAttribute label="User Id">
        <HighlightableText result={result} path="user.userId" />
      </DetailAttribute>
    </DetailContainer>
  );
}
