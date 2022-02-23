import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { PetizioniResultItem } from "./PetizioniItem";
import {
  DetailContainer,
  DetailFavicon,
  DetailLink,
  DetailTitle,
  HighlightableText,
  ResultAttribute,
} from "../../../renderer-components";

type PetizioniDetailProps = {
  result: GenericResultItem<PetizioniResultItem>;
};
export function PetizioniDetail({ result }: PetizioniDetailProps) {
  return (
    <DetailContainer>
      <DetailFavicon src={result.source.web.favicon} />
      <DetailTitle>
        <HighlightableText result={result} path="web.title" />
      </DetailTitle>
      <DetailLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </DetailLink>
      {result.source.petizioni.status && (
        <ResultAttribute label="Status">
          {result.source.petizioni.status}
        </ResultAttribute>
      )}
      {result.source.petizioni.pubDate && (
        <ResultAttribute label="Pubblication date">
          {result.source.petizioni.pubDate}
        </ResultAttribute>
      )}
    </DetailContainer>
  );
}
