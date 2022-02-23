import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFilePdf } from "@fortawesome/free-solid-svg-icons";
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
      <ResultTextContent result={result} path="document.content" />
    </ResultContainer>
  );
}
