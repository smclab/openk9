import React from "react";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailFavicon } from "../../../renderer-components/DetailFavicon";
import { DetailLink } from "../../../renderer-components/DetailLink";
import { DetailTextContent } from "../../../renderer-components/DetailTextContent";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { WebResultItem } from "./WebItem";

type WebDetailProps = {
  result: WebResultItem;
};
export function WebDetail({ result }: WebDetailProps) {
  return (
    <DetailContainer>
      <DetailFavicon src={result.source.web?.favicon} />
      <DetailTitle>
        <HighlightableText result={result} path="web.title" />
      </DetailTitle>
      <DetailLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </DetailLink>
      <DetailTextContent result={result} path="web.content" />
    </DetailContainer>
  );
}
