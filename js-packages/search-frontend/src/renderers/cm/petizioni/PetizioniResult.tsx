import React from "react";
import { Badge } from "../../../renderer-components/Badge";
import { PetizioniResultItem } from "./PetizioniItem";
import { ResultContainer } from "../../../renderer-components/ResultContainer";
import { ResultFavicon } from "../../../renderer-components/ResultFavicon";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { ResultTitle } from "../../../renderer-components/ResultTitle";
import { ResultLink } from "../../../renderer-components/ResultLink";
import { ResultTextContent } from "../../../renderer-components/ResultTextContent";
import { BadgeContainer } from "../../../renderer-components/BadgeContainer";

type PetizioniResultProps = { result: PetizioniResultItem };
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
