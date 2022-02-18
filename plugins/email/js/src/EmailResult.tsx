import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faEnvelope } from "@fortawesome/free-solid-svg-icons";
import { EmailResultItem } from "./EmailItem";
import { GenericResultItem } from "@openk9/rest-api";
import { rendererComponents } from "@openk9/search-frontend";

const {
  ResultContainer,
  ResultTitle,
  HighlightableText,
  ResultAttribute,
  ResultTextContent,
} = rendererComponents;

type EmailResultProps = { result: GenericResultItem<EmailResultItem> };
export function EmailResult({ result }: EmailResultProps) {
  return (
    <ResultContainer icon={<FontAwesomeIcon icon={faEnvelope} />}>
      <ResultTitle>
        <HighlightableText result={result} path="email.subject" />
      </ResultTitle>
      <ResultAttribute label="Date">
        {datetimeFormatter.format(result.source.email.date)}
      </ResultAttribute>
      <ResultAttribute label="From">
        <HighlightableText result={result} path="email.from" />
      </ResultAttribute>
      <ResultAttribute label="To">
        <HighlightableText result={result} path="email.to" />
      </ResultAttribute>
      <ResultTextContent result={result} path="email.body" />
    </ResultContainer>
  );
}

const datetimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: "full",
  timeStyle: "medium",
});
