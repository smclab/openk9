import React from "react";
import { DetailAttribute } from "../../../renderer-components/DetailAttribute";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailHeaderImage } from "../../../renderer-components/DetailHeaderImage";
import { DetailLink } from "../../../renderer-components/DetailLink";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { ProcessiResultItem } from "./ProcessiItem";

type ProcessiDetailProps = {
  result: ProcessiResultItem;
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
