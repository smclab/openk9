import React from "react";
import { DetailAttribute } from "../../../renderer-components/DetailAttribute";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailHeaderImage } from "../../../renderer-components/DetailHeaderImage";
import { DetailLink } from "../../../renderer-components/DetailLink";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { MostreResultItem } from "./MostreItem";

type MostreDetailProps = {
  result: MostreResultItem;
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
