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
    </DetailContainer>
  );
}
