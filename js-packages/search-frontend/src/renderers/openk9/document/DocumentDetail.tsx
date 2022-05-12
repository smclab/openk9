import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { DocumentResultItem } from "./DocumentItem";
import { faFileAlt } from "@fortawesome/free-solid-svg-icons/faFileAlt";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  DetailContainer,
  DetailIconContainer,
  DetailLink,
  DetailTextContent,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type DocumentDetailProps = {
  result: GenericResultItem<DocumentResultItem>;
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
