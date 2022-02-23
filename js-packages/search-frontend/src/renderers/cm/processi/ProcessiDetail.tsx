import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { ProcessiResultItem } from "./ProcessiItem";
import {
  DetailAttribute,
  DetailContainer,
  DetailHeaderImage,
  DetailLink,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type ProcessiDetailProps = {
  result: GenericResultItem<ProcessiResultItem>;
};
export function ProcessiDetail({ result }: ProcessiDetailProps) {
  return (
    <DetailContainer>
      {result.source.processi.imgUrl && (
        <DetailHeaderImage src={result.source.processi.imgUrl} />
      )}
      <DetailTitle>
        <HighlightableText result={result} path="web.title" />
      </DetailTitle>
      <DetailLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </DetailLink>
      {result.source.processi.name && (
        <div>
          <strong>Name</strong> : {result.source.processi.name}
        </div>
      )}
      {result.source.processi.startDate && (
        <DetailAttribute label="Start date">
          {result.source.processi.startDate}
        </DetailAttribute>
      )}
      {result.source.processi.endDate && (
        <DetailAttribute label="End date">
          {result.source.processi.endDate}
        </DetailAttribute>
      )}
      {result.source.processi.partecipants && (
        <DetailAttribute label="Partecipants">
          {result.source.processi.partecipants}
        </DetailAttribute>
      )}
      {result.source.processi.area && (
        <DetailAttribute label="Area">
          {result.source.processi.area}
        </DetailAttribute>
      )}
    </DetailContainer>
  );
}
