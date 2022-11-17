import React from "react";
import { GenericResultItem } from "../../../components/client";
import { WebResultItem } from "./WebItem";
import {
  DetailContainer,
  DetailFavicon,
  DetailLink,
  DetailTextContent,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type WebDetailProps = {
  result: GenericResultItem<WebResultItem>;
};
export function WebDetail({ result }: WebDetailProps) {
  return (
    <DetailContainer>
      <DetailFavicon src={result.source.web.favicon} />
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
