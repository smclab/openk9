import React from "react";
import { DetailAttribute } from "../../../renderer-components/DetailAttribute";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailFavicon } from "../../../renderer-components/DetailFavicon";
import { DetailLink } from "../../../renderer-components/DetailLink";
import { DetailTextContent } from "../../../renderer-components/DetailTextContent";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { AssistenzaResultItem } from "./AssistenzaItem";

type AssistenzaDetailProps = {
  result: AssistenzaResultItem;
};
export function AssistenzaDetail({ result }: AssistenzaDetailProps) {
  return (
    <DetailContainer>
      <DetailFavicon src={result.source.web.favicon} />
      <DetailTitle>
        <HighlightableText result={result} path="web.title" />
      </DetailTitle>
      <DetailLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </DetailLink>
      <DetailAttribute label="Topics">
        <ul>
          {result.source.topic.topics.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </DetailAttribute>
      <DetailTextContent result={result} path="web.content" />
    </DetailContainer>
  );
}
