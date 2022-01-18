import React from "react";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailFavicon } from "../../../renderer-components/DetailFavicon";
import { DetailLink } from "../../../renderer-components/DetailLink";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { ResultAttribute } from "../../../renderer-components/ResultAttribute";
import { PetizioniResultItem } from "./PetizioniItem";

type PetizioniDetailProps = {
  result: PetizioniResultItem;
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
