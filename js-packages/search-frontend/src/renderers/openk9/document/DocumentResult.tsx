import React from "react";
import { css } from "styled-components/macro";
import { truncatedLineStyle } from "../../../renderer-components/truncatedLineStyle";
import { HighlightedText } from "../../../renderer-components/HighlightedText";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFileAlt } from "@fortawesome/free-solid-svg-icons";
import { DocumentResultItem } from "./DocumentItem";
import { ResultTitle } from "../../../renderer-components/ResultTitle";
import { ResultContainer } from "../../../renderer-components/ResultContainer";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { ResultLink } from "../../../renderer-components/ResultLink";
import { ResultTextContent } from "../../../renderer-components/ResultTextContent";

type DocumentResultProps = { result: DocumentResultItem };
export function DocumentResult({ result }: DocumentResultProps) {
  return (
    <ResultContainer icon={<FontAwesomeIcon icon={faFileAlt} />}>
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
