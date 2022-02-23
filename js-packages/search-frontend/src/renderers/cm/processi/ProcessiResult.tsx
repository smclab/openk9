import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { ProcessiResultItem } from "./ProcessiItem";
import {
  Badge,
  ResultContainer,
  ResultFavicon,
  ResultTitle,
  HighlightableText,
  ResultLink,
  ResultTextContent,
  BadgeContainer,
} from "../../../renderer-components";

type ProcessiResultProps = { result: GenericResultItem<ProcessiResultItem> };
export function ProcessiResult({ result }: ProcessiResultProps) {
  return (
    <ResultContainer icon={<ResultFavicon src={result.source.web.favicon} />}>
      <ResultTitle>
        <HighlightableText result={result} path="web.title" />
      </ResultTitle>
      <ResultLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </ResultLink>
      <BadgeContainer>
        {result.source.processi.name && (
          <Badge>{result.source.processi.name}</Badge>
        )}
      </BadgeContainer>
      <ResultTextContent result={result} path="web.content" />
    </ResultContainer>
  );
}
