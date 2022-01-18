import React from "react";
import { Badge } from "../../../renderer-components/Badge";
import { OpendataResultItem } from "./OpendataItem";
import { ResultFavicon } from "../../../renderer-components/ResultFavicon";
import { ResultContainer } from "../../../renderer-components/ResultContainer";
import { ResultTitle } from "../../../renderer-components/ResultTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { ResultLink } from "../../../renderer-components/ResultLink";
import { ResultTextContent } from "../../../renderer-components/ResultTextContent";
import { BadgeContainer } from "../../../renderer-components/BadgeContainer";

type OpendataResultProps = { result: OpendataResultItem };
export function OpendataResult({ result }: OpendataResultProps) {
  return (
    <ResultContainer icon={<ResultFavicon src={result.source.web?.favicon} />}>
      <ResultTitle>
        <HighlightableText result={result} path="web.title" />
      </ResultTitle>
      <ResultLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </ResultLink>
      <BadgeContainer>
        {result.source.opendata?.temiDelDataset?.map((item, index) => {
          return <Badge key={index}>{item}</Badge>;
        })}
      </BadgeContainer>
      <ResultTextContent result={result} path="web.content" />
    </ResultContainer>
  );
}
