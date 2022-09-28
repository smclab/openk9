import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { DocumentResultItem } from "./DocumentItem";
import {
  ResultTitle,
  ResultContainer,
  HighlightableText,
  ResultLink,
  ResultTextContent,
} from "../../../renderer-components";
import { FileIcon } from "../../../renderer-components/FileIcon";

type DocumentResultProps = { result: GenericResultItem<DocumentResultItem> };
export function DocumentResult({ result }: DocumentResultProps) {
  return (
    <ResultContainer icon={<FileIcon result={result}/>}>
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
