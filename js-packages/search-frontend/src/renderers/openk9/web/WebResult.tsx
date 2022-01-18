import React from "react";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { ResultTextContent } from "../../../renderer-components/ResultTextContent";
import { ResultContainer } from "../../../renderer-components/ResultContainer";
import { ResultFavicon } from "../../../renderer-components/ResultFavicon";
import { ResultLink } from "../../../renderer-components/ResultLink";
import { ResultTitle } from "../../../renderer-components/ResultTitle";
import { WebResultItem } from "./WebItem";

type WebResultProps = { result: WebResultItem };
export function WebResult({ result }: WebResultProps) {
  return (
    <ResultContainer icon={<ResultFavicon src={result.source.web.favicon} />}>
      <ResultTitle>
        <HighlightableText result={result} path="web.title" />
      </ResultTitle>
      <ResultLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </ResultLink>
      <ResultTextContent result={result} path="web.content" />
    </ResultContainer>
  );
}
