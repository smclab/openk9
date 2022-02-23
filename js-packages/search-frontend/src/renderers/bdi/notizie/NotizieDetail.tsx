import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { NotizieResultItem } from "./NotizieItem";
import {
  DetailAttribute,
  DetailContainer,
  DetailHeaderImage,
  DetailLink,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type NotizieDetailProps = {
  result: GenericResultItem<NotizieResultItem>;
};
export function NotizieDetail({ result }: NotizieDetailProps) {
  return (
    <DetailContainer>
      <DetailHeaderImage src={result.source.notizie.imgUrl} />
      <DetailTitle>
        <HighlightableText result={result} path="web.title" />
      </DetailTitle>
      <DetailLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </DetailLink>
      <DetailAttribute label="categories">
        <ul>
          {result.source.notizie.categories?.slice(0, 2).map((url) => {
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
      <DetailAttribute label="Pubblication Date">
        {result.source.notizie.pubDate}
      </DetailAttribute>
      <DetailAttribute label="Linked Documents">
        <ul>
          {result.source.notizie.linkedUrls?.slice(0, 3).map((url) => {
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
    </DetailContainer>
  );
}
