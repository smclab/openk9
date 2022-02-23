import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { CalendarResultItem } from "./CalendarItem";
import { faCalendar } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  DetailAttribute,
  DetailContainer,
  DetailIconContainer,
  DetailTitle,
  HighlightableText,
} from "../../../renderer-components";

type CalendarDetailProps = {
  result: GenericResultItem<CalendarResultItem>;
};
export function CalendarDetail({ result }: CalendarDetailProps) {
  return (
    <DetailContainer>
      <DetailIconContainer>
        <FontAwesomeIcon icon={faCalendar} />
      </DetailIconContainer>
      <DetailTitle>
        <HighlightableText result={result} path="calendar.title" />
      </DetailTitle>
      {result.source.calendar.description && (
        <DetailAttribute label="Description">
          <HighlightableText result={result} path="calendar.description" />
        </DetailAttribute>
      )}
      <DetailAttribute label="Start">
        <HighlightableText result={result} path="calendar.startTime" />
      </DetailAttribute>
      <DetailAttribute label="End">
        <HighlightableText result={result} path="calendar.endTime" />
      </DetailAttribute>
      {result.source.calendar.location && (
        <DetailAttribute label="Location">
          <HighlightableText result={result} path="calendar.location" />
        </DetailAttribute>
      )}
    </DetailContainer>
  );
}
