import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { PubblicazioniResultItem } from "./PubblicazioniItem";
import {
  Badge,
  ResultContainer,
  ResultTitle,
  HighlightableText,
  ResultLink,
  ResultTextContent,
  ResultFavicon,
  BadgeContainer,
} from "../../../renderer-components";

type PubblicazioniResultProps = {
  result: GenericResultItem<PubblicazioniResultItem>;
};
export function PubblicazioniResult({ result }: PubblicazioniResultProps) {
  return (
    <ResultContainer icon={<ResultFavicon src={result.source.web.favicon} />}>
      <ResultTitle>
        <HighlightableText result={result} path="web.title" />
      </ResultTitle>
      <ResultLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </ResultLink>
      <BadgeContainer>
        {result.source.pubblicazioni.pubDate && (
          <Badge>{result.source.pubblicazioni.pubDate}</Badge>
        )}
        {result.source.pubblicazioni.topic && (
          <Badge>{result.source.pubblicazioni.topic}</Badge>
        )}
      </BadgeContainer>
      <ResultTextContent result={result} path="web.content" />
    </ResultContainer>
  );
}
