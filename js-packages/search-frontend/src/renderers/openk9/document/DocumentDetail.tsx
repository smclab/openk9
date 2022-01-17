import { faFileAlt } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import { css } from "styled-components/macro";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailIconContainer } from "../../../renderer-components/DetailIconContainer";
import { DetailLink } from "../../../renderer-components/DetailLink";
import { DetailTextContent } from "../../../renderer-components/DetailTextContent";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { HighlightedText } from "../../../renderer-components/HighlightedText";
import { DocumentResultItem } from "./DocumentItem";

type DocumentDetailProps = {
  result: DocumentResultItem;
};
export function DocumentDetail({ result }: DocumentDetailProps) {
  return (
    <DetailContainer>
      <DetailIconContainer>
        <FontAwesomeIcon icon={faFileAlt} />
      </DetailIconContainer>
      <DetailTitle>
        <HighlightableText result={result} path="document.title" />
      </DetailTitle>
      <DetailLink href={result.source.document.url}>
        <HighlightableText result={result} path="document.url" />
      </DetailLink>
      <DetailTextContent result={result} path="document.content" />
    </DetailContainer>
  );
}
