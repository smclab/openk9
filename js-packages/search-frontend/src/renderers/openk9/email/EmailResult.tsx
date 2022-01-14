import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faEnvelope } from "@fortawesome/free-solid-svg-icons";
import { EmailResultItem } from "./EmailItem";
import { ResultContainer } from "../../../renderer-components/ResultContainer";
import { ResultTitle } from "../../../renderer-components/ResultTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { ResultAttribute } from "../../../renderer-components/ResultAttribute";
import { ResultTextContent } from "../../../renderer-components/ResultTextContent";

type EmailResultProps = { result: EmailResultItem };
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
