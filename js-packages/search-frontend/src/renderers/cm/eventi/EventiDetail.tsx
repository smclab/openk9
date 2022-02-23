import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { EventiResultItem } from "./EventiItem";
import {
  DetailAttribute,
  DetailContainer,
  DetailHeaderImage,
  DetailLink,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type EventiDetailProps = {
  result: GenericResultItem<EventiResultItem>;
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
