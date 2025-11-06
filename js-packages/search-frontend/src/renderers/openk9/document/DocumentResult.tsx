import React from "react";
import { GenericResultItem } from "../../../components/client";
import { DocumentResultItem } from "./DocumentItem";
import {
  ResultTitle,
  ResultContainer,
  HighlightableText,
  ResultLink,
  ResultTextContent,
} from "../../../renderer-components";
import { FileIcon } from "../../../renderer-components/FileIcon";
import { css } from "styled-components/macro";

type DocumentResultProps = { result: GenericResultItem<DocumentResultItem> };
export function DocumentResult({ result }: DocumentResultProps) {
  return (
    <ResultContainer icon={<FileIcon result={result} />}>
      <ResultTitle>
        <HighlightableText result={result} path="document.title" />
      </ResultTitle>
      <ResultLink href={result.source.document.url}>
        <HighlightableText result={result} path="document.url" />
      </ResultLink>
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
          <ResultTextContent result={result} path="document.summary" />
        </div>
      )}
    </ResultContainer>
  );
}
