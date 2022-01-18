import React from "react";
import { Badge } from "../../../renderer-components/Badge";
import { ProcessiResultItem } from "./ProcessiItem";
import { ResultContainer } from "../../../renderer-components/ResultContainer";
import { ResultFavicon } from "../../../renderer-components/ResultFavicon";
import { ResultTitle } from "../../../renderer-components/ResultTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { ResultLink } from "../../../renderer-components/ResultLink";
import { ResultTextContent } from "../../../renderer-components/ResultTextContent";
import { BadgeContainer } from "../../../renderer-components/BadgeContainer";

type ProcessiResultProps = { result: ProcessiResultItem };
export function ProcessiResult({ result }: ProcessiResultProps) {
  return (
    <ResultContainer icon={<ResultFavicon src={result.source.web.favicon} />}>
      <ResultTitle>
        <HighlightableText result={result} path="web.title" />
      </ResultTitle>
      <ResultLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </ResultLink>
      <BadgeContainer>
        {result.source.processi.name && (
          <Badge>{result.source.processi.name}</Badge>
        )}
      </BadgeContainer>
      <ResultTextContent result={result} path="web.content" />
    </ResultContainer>
  );
}
