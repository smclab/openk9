import React from "react";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { ResultContainer } from "../../../renderer-components/ResultContainer";
import { ResultTitle } from "../../../renderer-components/ResultTitle";
import { CalendarResultItem } from "./CalendarItem";
import { faCalendar } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { ResultAttribute } from "../../../renderer-components/ResultAttribute";

type CalendarResultProps = { result: CalendarResultItem };
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
