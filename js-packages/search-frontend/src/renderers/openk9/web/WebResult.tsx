import React from "react";
import { WebResultItem } from "./WebItem";
import { GenericResultItem } from "@openk9/rest-api";
import {
  HighlightableText,
  ResultTextContent,
  ResultContainer,
  ResultFavicon,
  ResultLink,
  ResultTitle,
} from "../../../renderer-components";

type WebResultProps = { result: GenericResultItem<WebResultItem> };
export function WebResult({ result }: WebResultProps) {
  return (
    <ResultContainer icon={<ResultFavicon src={result.source.web.favicon} />}>
      <ResultTitle>
        <HighlightableText result={result} path="web.title" />
      </ResultTitle>
      <ResultLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </ResultLink>
      <ResultTextContent result={result} path="web.content" />
    </ResultContainer>
  );
}
