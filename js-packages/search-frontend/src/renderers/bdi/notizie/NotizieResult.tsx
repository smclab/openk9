import React from "react";
import { Badge } from "../../../renderer-components/Badge";
import { NotizieResultItem } from "./NotizieItem";
import { ResultContainer } from "../../../renderer-components/ResultContainer";
import { ResultFavicon } from "../../../renderer-components/ResultFavicon";
import { ResultTitle } from "../../../renderer-components/ResultTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { ResultLink } from "../../../renderer-components/ResultLink";
import { BadgeContainer } from "../../../renderer-components/BadgeContainer";
import { ResultTextContent } from "../../../renderer-components/ResultTextContent";

type NotizieResultProps = { result: NotizieResultItem };
export function NotizieResult({ result }: NotizieResultProps) {
  return (
    <ResultContainer icon={<ResultFavicon src={result.source.web?.favicon} />}>
      <ResultTitle>
        <HighlightableText result={result} path="web.title" />
      </ResultTitle>
      <ResultLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </ResultLink>
      <BadgeContainer>
        {result.source.notizie?.pubDate && (
          <Badge>{result.source.notizie.pubDate}</Badge>
        )}
        {result.source.notizie?.topic && (
          <Badge>{result.source.notizie.topic}</Badge>
        )}
      </BadgeContainer>
      <ResultTextContent result={result} path="web.content" />
    </ResultContainer>
  );
}
