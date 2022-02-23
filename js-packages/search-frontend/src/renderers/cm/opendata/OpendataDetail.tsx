import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { OpendataResultItem } from "./OpendataItem";
import {
  DetailAttribute,
  DetailContainer,
  DetailFavicon,
  DetailLink,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type OpendataDetailProps = {
  result: GenericResultItem<OpendataResultItem>;
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
