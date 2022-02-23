import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { EntrateResultItem } from "./EntrateItem";
import {
  DetailAttribute,
  DetailContainer,
  DetailFavicon,
  DetailLink,
  DetailTextContent,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type EntrateDetailProps = {
  result: GenericResultItem<EntrateResultItem>;
};
export function EntrateDetail({ result }: EntrateDetailProps) {
  return (
    <DetailContainer>
      <DetailFavicon src={result.source.web.favicon} />
      <DetailTitle>
        <HighlightableText result={result} path="web.title" />
      </DetailTitle>
      <DetailLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </DetailLink>
      <DetailAttribute label="Category">
        {result.source.entrate.category}
      </DetailAttribute>
      <DetailAttribute label="Topics">
        <ul>
          {result.source.topic.topics.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </DetailAttribute>
      <DetailAttribute label="Linked Documents">
        <ul>
          {result.source.entrate.linkedUrls.slice(0, 3).map((url) => {
            return (
              <li key={url}>
                <a
                  href={url}
                  target="_blank"
                  rel="noreferrer"
                  style={{ wordBreak: "break-all" }}
                >
                  {url}
                </a>
              </li>
            );
          })}
        </ul>
      </DetailAttribute>
      <DetailTextContent result={result} path="web.content" />
    </DetailContainer>
  );
}
