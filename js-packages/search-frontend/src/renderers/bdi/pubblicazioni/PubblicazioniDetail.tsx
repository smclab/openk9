import React from "react";
import { css } from "styled-components/macro";
import { DetailAttribute } from "../../../renderer-components/DetailAttribute";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailHeaderImage } from "../../../renderer-components/DetailHeaderImage";
import { DetailLink } from "../../../renderer-components/DetailLink";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { PubblicazioniResultItem } from "./PubblicazioniItem";

type PubblicazioniDetailProps = {
  result: PubblicazioniResultItem;
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
        {result.source.pubblicazioni?.category}
      </DetailAttribute>
      <DetailAttribute label="Topic">
        {result.source.pubblicazioni?.topic}
      </DetailAttribute>
      <DetailAttribute label="Authors">
        {result.source.pubblicazioni?.authors}
      </DetailAttribute>
      <DetailAttribute label="Pubblication Date">
        {result.source.pubblicazioni?.pubDate}
      </DetailAttribute>
      <DetailAttribute label="Linked Urls">
        <ul>
          {result.source.pubblicazioni?.linkedUrls.slice(0, 3).map((url) => {
            return (
              <li key={url}>
                <a
                  href={url}
                  target="_blank"
                  rel="noreferrer"
                  css={css`
                    word-break: break-all;
                  `}
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
