import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { MostreResultItem } from "./MostreItem";
import {
  Badge,
  ResultContainer,
  ResultFavicon,
  ResultTitle,
  HighlightableText,
  ResultLink,
  BadgeContainer,
  ResultTextContent,
} from "../../../renderer-components";

type MostreResultProps = { result: GenericResultItem<MostreResultItem> };
export function MostreResult({ result }: MostreResultProps) {
  return (
    <ResultContainer icon={<ResultFavicon src={result.source.web?.favicon} />}>
      <ResultTitle>
        <HighlightableText result={result} path="web.title" />
      </ResultTitle>
      <ResultLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </ResultLink>
      <BadgeContainer>
        <Badge>{result.source.mostre.location}</Badge>
      </BadgeContainer>
      <ResultTextContent result={result} path="web.content" />
    </ResultContainer>
  );
}
