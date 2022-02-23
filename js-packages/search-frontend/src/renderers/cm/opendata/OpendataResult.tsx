import React from "react";
import { OpendataResultItem } from "./OpendataItem";
import { GenericResultItem } from "@openk9/rest-api";
import {
  Badge,
  ResultFavicon,
  ResultContainer,
  ResultTitle,
  HighlightableText,
  ResultLink,
  ResultTextContent,
  BadgeContainer,
} from "../../../renderer-components";

type OpendataResultProps = { result: GenericResultItem<OpendataResultItem> };
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
