import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { EventiResultItem } from "./EventiItem";
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

type EventiResultProps = { result: GenericResultItem<EventiResultItem> };
export function EventiResult({ result }: EventiResultProps) {
  return (
    <ResultContainer icon={<ResultFavicon src={result.source.web?.favicon} />}>
      <ResultTitle>
        <HighlightableText result={result} path="web.title" />
      </ResultTitle>
      <ResultLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </ResultLink>
      <BadgeContainer>
        <Badge>{result.source.eventi.location}</Badge>
      </BadgeContainer>
      <ResultTextContent result={result} path="web.content" />
    </ResultContainer>
  );
}
