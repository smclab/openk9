import React from "react";
import { faEnvelope } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { GenericResultItem } from "@openk9/rest-api";
import { EmailResultItem } from "./EmailItem";
import { rendererComponents } from "@openk9/search-frontend";

const {
  DetailTextContent,
  DetailContainer,
  DetailIconContainer,
  DetailTitle,
  HighlightableText,
  DetailAttribute,
} = rendererComponents;

type EmailDetailProps = {
  result: GenericResultItem<EmailResultItem>;
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
      {result.source.email.cc && (
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
          style={{
            width: "100%",
            border: "1px solid var(--openk9-embeddable-search--border-color)",
            borderRadius: "4px",
            backgroundColor: "white",
          }}
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
