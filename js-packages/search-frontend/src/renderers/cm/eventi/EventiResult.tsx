import React from "react";
import { Badge } from "../../../renderer-components/Badge";
import { EventiResultItem } from "./EventiItem";
import { ResultContainer } from "../../../renderer-components/ResultContainer";
import { ResultFavicon } from "../../../renderer-components/ResultFavicon";
import { ResultTitle } from "../../../renderer-components/ResultTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { ResultLink } from "../../../renderer-components/ResultLink";
import { ResultTextContent } from "../../../renderer-components/ResultTextContent";
import { BadgeContainer } from "../../../renderer-components/BadgeContainer";

type EventiResultProps = { result: EventiResultItem };
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
