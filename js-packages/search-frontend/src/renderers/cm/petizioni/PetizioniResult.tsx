import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { PetizioniResultItem } from "./PetizioniItem";
import {
  Badge,
  ResultContainer,
  ResultFavicon,
  HighlightableText,
  ResultTitle,
  ResultLink,
  ResultTextContent,
  BadgeContainer,
} from "../../../renderer-components";

type PetizioniResultProps = { result: GenericResultItem<PetizioniResultItem> };
export function PetizioniResult({ result }: PetizioniResultProps) {
  return (
    <ResultContainer icon={<ResultFavicon src={result.source.web.favicon} />}>
      <ResultTitle>
        <HighlightableText result={result} path="web.title" />
      </ResultTitle>
      <ResultLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </ResultLink>
      <BadgeContainer>
        {result.source.petizioni.status && (
          <Badge>{result.source.petizioni.status}</Badge>
        )}
      </BadgeContainer>
      <ResultTextContent result={result} path="web.content" />
    </ResultContainer>
  );
}
