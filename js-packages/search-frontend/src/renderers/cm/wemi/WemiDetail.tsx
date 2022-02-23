import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { WemiResultItem } from "./WemiItem";
import {
  DetailAttribute,
  DetailContainer,
  DetailFavicon,
  DetailLink,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type WemiDetailProps = {
  result: GenericResultItem<WemiResultItem>;
};
export function WemiDetail({ result }: WemiDetailProps) {
  return (
    <DetailContainer>
      <DetailFavicon src={result.source.web.favicon} />
      <DetailTitle>
        <HighlightableText result={result} path="web.title" />
      </DetailTitle>
      <DetailLink href={result.source.web.url}>
        <HighlightableText result={result} path="web.url" />
      </DetailLink>
      <DetailAttribute label="Categoria">
        {result.source.wemi.categoria}
      </DetailAttribute>
      <DetailAttribute label="Servizio">
        {result.source.wemi.servizio}
      </DetailAttribute>
      <DetailAttribute label="Destinatari">
        <ul>
          {result.source.wemi.destinatari.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </DetailAttribute>
      <DetailAttribute label="Attività">
        <ul>
          {result.source.wemi.attivita.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </DetailAttribute>
      <DetailAttribute label="Prezzi">
        <ul>
          {result.source.wemi?.prezzi?.map((item, index) => {
            return (
              <li key={index}>
                {item.label}: {item.value}
              </li>
            );
          })}
        </ul>
      </DetailAttribute>
      <DetailAttribute label="Momento">
        <ul>
          {result.source.wemi.momento.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </DetailAttribute>
      <DetailAttribute label="Sedi">
        <ul>
          {result.source.wemi.sedi.map((item, index) => {
            return <li key={index}>{item}</li>;
          })}
        </ul>
      </DetailAttribute>
      <DetailAttribute label="Procedura">
        {result.source.wemi.procedura}
      </DetailAttribute>
    </DetailContainer>
  );
}
