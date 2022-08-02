import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFilePdf } from "@fortawesome/free-solid-svg-icons/faFilePdf";
import { GenericResultItem } from "@openk9/rest-api";
import { PdfResultItem } from "./PdfItem";
import {
  ResultContainer,
  ResultTitle,
  HighlightableText,
  ResultLink,
  ResultTextContent,
} from "../../../renderer-components";

type PdfResultProps = { result: GenericResultItem<PdfResultItem> };
export function PdfResult({ result }: PdfResultProps) {
  return (
    <ResultContainer icon={<FontAwesomeIcon icon={faFilePdf} />}>
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
          <DetailTextContent result={result} path="document.summary" />
        </div>
      )}
    </ResultContainer>
  );
}
