import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { AssistenzaResultItem } from "./AssistenzaItem";
import {
  DetailAttribute,
  DetailContainer,
  DetailFavicon,
  DetailLink,
  DetailTextContent,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type AssistenzaDetailProps = {
  result: GenericResultItem<AssistenzaResultItem>;
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
