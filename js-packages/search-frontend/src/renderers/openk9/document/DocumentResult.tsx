import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { DocumentResultItem } from "./DocumentItem";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFileAlt } from "@fortawesome/free-solid-svg-icons/faFileAlt";
import {
  ResultTitle,
  ResultContainer,
  HighlightableText,
  ResultLink,
  ResultTextContent,
} from "../../../renderer-components";

type DocumentResultProps = { result: GenericResultItem<DocumentResultItem> };
export function DocumentResult({ result }: DocumentResultProps) {
  return (
    <ResultContainer icon={<FontAwesomeIcon icon={faFileAlt} />}>
      <ResultTitle>
        <HighlightableText result={result} path="document.title" />
      </ResultTitle>
      <ResultLink href={result.source.document.url}>
        <HighlightableText result={result} path="document.url" />
      </ResultLink>
      {'document.content' in result.highlight ? (
        <div
          style={{
            marginTop: "8px",
            maxWidth: "100%",
            lineHeight: "1em",
            maxHeight: "6em",
            overflow: "hidden",
            textOverflow: "ellipsis",
            wordWrap: "break-word",
            wordBreak: "break-word",
          }}
        >
          <HighlightableText result={result} path="document.content" />
        </div>
      ) : (
        <div
          style={{
            marginTop: "8px",
            maxWidth: "100%",
            lineHeight: "1em",
            maxHeight: "6em",
            overflow: "hidden",
            textOverflow: "ellipsis",
            wordWrap: "break-word",
            wordBreak: "break-word",
          }}
        >
          <ResultTextContent result={result} path="document.summary" />
        </div>
      )}
    </ResultContainer>
  );
}
