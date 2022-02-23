import React from "react";
import { MostreResultItem } from "./MostreItem";
import { GenericResultItem } from "@openk9/rest-api";
import {
  DetailAttribute,
  DetailContainer,
  DetailHeaderImage,
  DetailLink,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type MostreDetailProps = {
  result: GenericResultItem<MostreResultItem>;
};
export function MostreDetail({ result }: MostreDetailProps) {
  return (
    <DetailContainer>
      <DetailHeaderImage src={result.source.mostre?.imgUrl} />
      <DetailTitle>
        <HighlightableText result={result} path="web.title" />
      </DetailTitle>
      <DetailLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </DetailLink>
      <DetailAttribute label="Start Date">
        {result.source.mostre.startDate}
      </DetailAttribute>
      <DetailAttribute label="End Date">
        {result.source.mostre.endDate}
      </DetailAttribute>
      <DetailAttribute label="Location">
        {result.source.mostre.location}
      </DetailAttribute>
    </DetailContainer>
  );
}
