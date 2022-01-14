import { faEnvelope } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import { css } from "styled-components/macro";
import { myTheme } from "../../../components/myTheme";
import { EmailResultItem } from "./EmailItem";
import { DetailTextContent } from "../../../renderer-components/DetailTextContent";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailIconContainer } from "../../../renderer-components/DetailIconContainer";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { DetailAttribute } from "../../../renderer-components/DetailAttribute";

type EmailDetailProps = {
  result: EmailResultItem;
};
export function EmailDetail({ result }: EmailDetailProps) {
  return (
    <DetailContainer>
      <DetailIconContainer>
        <FontAwesomeIcon icon={faEnvelope} />
      </DetailIconContainer>
      <DetailTitle>
        <HighlightableText result={result} path="email.subject" />
      </DetailTitle>
      <DetailAttribute label="Date">
        {datetimeFormatter.format(result.source.email.date)}
      </DetailAttribute>
      <DetailAttribute label="From">
        <HighlightableText result={result} path="email.from" />
      </DetailAttribute>
      <DetailAttribute label="To">
        <HighlightableText result={result} path="email.to" />
      </DetailAttribute>
      {result.source.email?.cc && (
        <DetailAttribute label="Cc">
          <HighlightableText result={result} path="email.cc" />
        </DetailAttribute>
      )}
      {result.highlight["email.body"] && (
        <DetailTextContent result={result} path="email.body" />
      )}
      {result.source.email.htmlBody && (
        <iframe
          title={result.source.contentId}
          srcDoc={result.source.email.htmlBody}
          css={css`
            width: 100%;
            border: 1px solid ${myTheme.searchBarBorderColor};
            border-radius: 4px;
            background-color: white;
          `}
          onLoad={(event) => {
            event.currentTarget.style.height =
              (event.currentTarget.contentWindow as Window).document.body
                .scrollHeight +
              40 +
              "px";
          }}
        />
      )}
    </DetailContainer>
  );
}

const datetimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: "full",
  timeStyle: "medium",
});
