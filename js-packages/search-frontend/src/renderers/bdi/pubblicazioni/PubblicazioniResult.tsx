import React from "react";
import { Badge } from "../../../renderer-components/Badge";
import { PubblicazioniResultItem } from "./PubblicazioniItem";
import { ResultContainer } from "../../../renderer-components/ResultContainer";
import { ResultTitle } from "../../../renderer-components/ResultTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { ResultLink } from "../../../renderer-components/ResultLink";
import { ResultTextContent } from "../../../renderer-components/ResultTextContent";
import { ResultFavicon } from "../../../renderer-components/ResultFavicon";
import { BadgeContainer } from "../../../renderer-components/BadgeContainer";

type PubblicazioniResultProps = { result: PubblicazioniResultItem };
export function PubblicazioniResult({ result }: PubblicazioniResultProps) {
  return (
    <ResultContainer icon={<ResultFavicon src={result.source.web?.favicon} />}>
      <ResultTitle>
        <HighlightableText result={result} path="web.title" />
      </ResultTitle>
      <ResultLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </ResultLink>
      <BadgeContainer>
        {result.source.pubblicazioni?.pubDate && (
          <Badge>{result.source.pubblicazioni.pubDate}</Badge>
        )}
        {result.source.pubblicazioni?.topic && (
          <Badge>{result.source.pubblicazioni.topic}</Badge>
        )}
      </BadgeContainer>
      <ResultTextContent result={result} path="web.content" />
    </ResultContainer>
  );
}
