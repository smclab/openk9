import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { PubblicazioniResultItem } from "./PubblicazioniItem";
import {
  DetailAttribute,
  DetailContainer,
  DetailHeaderImage,
  DetailLink,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type PubblicazioniDetailProps = {
  result: GenericResultItem<PubblicazioniResultItem>;
};
export function PubblicazioniDetail({ result }: PubblicazioniDetailProps) {
  return (
    <DetailContainer>
      <DetailHeaderImage src={result.source.pubblicazioni?.imgUrl} />
      <DetailTitle>
        <HighlightableText result={result} path="web.title" />
      </DetailTitle>
      <DetailLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </DetailLink>
      <DetailAttribute label="Category">
        {result.source.pubblicazioni.category}
      </DetailAttribute>
      <DetailAttribute label="Topic">
        {result.source.pubblicazioni.topic}
      </DetailAttribute>
      <DetailAttribute label="Authors">
        {result.source.pubblicazioni.authors}
      </DetailAttribute>
      <DetailAttribute label="Pubblication Date">
        {result.source.pubblicazioni.pubDate}
      </DetailAttribute>
      <DetailAttribute label="Linked Urls">
        <ul>
          {result.source.pubblicazioni.linkedUrls.slice(0, 3).map((url) => {
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
