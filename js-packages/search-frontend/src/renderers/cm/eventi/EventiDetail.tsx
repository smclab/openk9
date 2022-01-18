import React from "react";
import { DetailAttribute } from "../../../renderer-components/DetailAttribute";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailHeaderImage } from "../../../renderer-components/DetailHeaderImage";
import { DetailLink } from "../../../renderer-components/DetailLink";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { EventiResultItem } from "./EventiItem";

type EventiDetailProps = {
  result: EventiResultItem;
};
export function EventiDetail({ result }: EventiDetailProps) {
  return (
    <DetailContainer>
      <DetailHeaderImage src={result.source.eventi?.imgUrl} />
      <DetailTitle>
        <HighlightableText result={result} path="web.title" />
      </DetailTitle>
      <DetailLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </DetailLink>
      {result.source.eventi?.date && (
        <DetailAttribute label="Date">
          {result.source.eventi.date}
        </DetailAttribute>
      )}
      {result.source.eventi?.startDate && (
        <DetailAttribute label="Start Date">
          {result.source.eventi.startDate}
        </DetailAttribute>
      )}
      {result.source.eventi?.endDate && (
        <DetailAttribute label="End Date">
          {result.source.eventi.endDate}
        </DetailAttribute>
      )}
      {result.source.eventi?.category && (
        <DetailAttribute label="Category">
          {result.source.eventi.category}
        </DetailAttribute>
      )}
      {result.source.eventi?.location && (
        <DetailAttribute label="Location">
          {result.source.eventi.location}
        </DetailAttribute>
      )}
    </DetailContainer>
  );
}
