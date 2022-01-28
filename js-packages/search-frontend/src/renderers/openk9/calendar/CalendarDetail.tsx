import { faCalendar, faUser } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import { DetailAttribute } from "../../../renderer-components/DetailAttribute";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailIconContainer } from "../../../renderer-components/DetailIconContainer";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { CalendarResultItem } from "./CalendarItem";

type CalendarDetailProps = {
  result: CalendarResultItem;
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
