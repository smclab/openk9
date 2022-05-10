import React from "react";
import { NotizieResultItem } from "./NotizieItem";
import { GenericResultItem } from "@openk9/rest-api";
import { rendererComponents } from "@openk9/search-frontend";

const {
  Badge,
  ResultContainer,
  ResultFavicon,
  ResultTitle,
  HighlightableText,
  ResultLink,
  BadgeContainer,
  ResultTextContent,
} = rendererComponents;

type NotizieResultProps = { result: GenericResultItem<NotizieResultItem> };
export function NotizieResult({ result }: NotizieResultProps) {
  return (
    <ResultContainer icon={<ResultFavicon src={result.source.web.favicon} />}>
      <ResultTitle>
        <HighlightableText result={result} path="web.title" />
      </ResultTitle>
      <ResultLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </ResultLink>
      <BadgeContainer>
        {result.source.notizie.pubDate && (
          <Badge>{result.source.notizie.pubDate}</Badge>
        )}
      </BadgeContainer>
      <ResultTextContent result={result} path="web.content" />
    </ResultContainer>
  );
}
