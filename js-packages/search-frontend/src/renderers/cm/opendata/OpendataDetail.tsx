import React from "react";
import { DetailAttribute } from "../../../renderer-components/DetailAttribute";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailFavicon } from "../../../renderer-components/DetailFavicon";
import { DetailLink } from "../../../renderer-components/DetailLink";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { OpendataResultItem } from "./OpendataItem";

type OpendataDetailProps = {
  result: OpendataResultItem;
};
export function OpendataDetail({ result }: OpendataDetailProps) {
  return (
    <DetailContainer>
      <DetailFavicon src={result.source.web.favicon} />
      <DetailTitle>
        <HighlightableText result={result} path="web.title" />
      </DetailTitle>
      <DetailLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </DetailLink>
      <DetailAttribute label="Data di inizio">
        {result.source.opendata.startDate}
      </DetailAttribute>
      <DetailAttribute label="Data di fine">
        {result.source.opendata.endDate}
      </DetailAttribute>
      <DetailAttribute label="Ultima data di modifica">
        {result.source.opendata.dataDiModifica}
      </DetailAttribute>
      <DetailAttribute label="Titolare">
        {result.source.opendata.titolare}
      </DetailAttribute>
      <DetailAttribute label="Copertura geografica">
        {result.source.opendata.coperturaGeografica}
      </DetailAttribute>
      <DetailAttribute label="Autore">
        {result.source.opendata.autore}
      </DetailAttribute>
      <DetailAttribute label="Temi del dataset">
        <ul>
          {result.source.opendata.temiDelDataset.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </DetailAttribute>
      <DetailAttribute label="Tags">
        <ul>
          {result.source.opendata.tags.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </DetailAttribute>
      <DetailAttribute label="Summary">
        {result.source.opendata.summary}
      </DetailAttribute>
    </DetailContainer>
  );
}
