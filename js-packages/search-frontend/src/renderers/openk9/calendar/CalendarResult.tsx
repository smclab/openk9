import React from "react";
import { GenericResultItem } from "@openk9/rest-api";
import { CalendarResultItem } from "./CalendarItem";
import { faCalendar } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  HighlightableText,
  ResultContainer,
  ResultTitle,
  ResultAttribute,
} from "../../../renderer-components";

type CalendarResultProps = { result: GenericResultItem<CalendarResultItem> };
export function CalendarResult({ result }: CalendarResultProps) {
  return (
    <ResultContainer icon={<FontAwesomeIcon icon={faCalendar} />}>
      <ResultTitle>
        <HighlightableText result={result} path="calendar.title" />
      </ResultTitle>
      <ResultAttribute label="Start">
        <HighlightableText result={result} path="calendar.startTime" />
      </ResultAttribute>
      <ResultAttribute label="End">
        <HighlightableText result={result} path="calendar.endTime" />
      </ResultAttribute>
    </ResultContainer>
  );
}
