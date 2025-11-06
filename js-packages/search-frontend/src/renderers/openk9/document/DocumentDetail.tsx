import React from "react";
import { GenericResultItem } from "../../../components/client";
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
import { css } from "styled-components/macro";

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
      {"document.content" in result.highlight ? (
        <div
          css={css`
            margin-top: 8px;
            max-width: 100%;
            line-height: 1em;
            max-height: 6em;
            overflow: hidden;
            text-overflow: ellipsis;
            word-wrap: break-word;
            word-break: break-word;
          `}
        >
          <HighlightableText result={result} path="document.content" />
        </div>
      ) : (
        <div
          css={css`
            margin-top: 8px;
            max-width: 100%;
            line-height: 1em;
            max-height: 6em;
            overflow: hidden;
            text-overflow: ellipsis;
            word-wrap: break-word;
            word-break: break-word;
          `}
        >
          <DetailTextContent result={result} path="document.summary" />
        </div>
      )}
    </DetailContainer>
  );
}
