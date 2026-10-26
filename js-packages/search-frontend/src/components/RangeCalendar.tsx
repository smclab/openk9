/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
// Range calendar with the rules of react-dates' DayPickerRangeController.
import React from "react";
import {
  Chevron as DayPickerChevron,
  ChevronProps,
  DayPicker,
  DayPickerProps,
} from "react-day-picker";
import { enUS } from "react-day-picker/locale";
import { differenceInCalendarDays, isSameDay } from "date-fns";
import {
  atNoon,
  blocksMinimumNights,
  chooseAvailablePhrase,
  dayAriaLabel,
  FocusedInput,
  inHoveredSpan,
  rangeClick,
  RangeDates,
} from "./dateRange";

// moment never got its locales under Vite: the calendars have always been in English
export const calendarLocale = enUS;

export const calendarLabels: DayPickerProps["labels"] = {
  labelPrevious: () => "Move backward to switch to the previous month.",
  labelNext: () => "Move forward to switch to the next month.",
};

// the arrows of react-dates
export function Chevron(props: ChevronProps) {
  if (props.orientation !== "left" && props.orientation !== "right") {
    return <DayPickerChevron {...props} />;
  }
  return (
    <svg className={props.className} focusable="false" viewBox="0 0 1000 1000">
      <path
        d={
          props.orientation === "left"
            ? "M336 275L126 485h806c13 0 23 10 23 23s-10 23-23 23H126l210 210c11 11 11 21 0 32-5 5-10 7-16 7s-11-2-16-7L55 524c-11-11-11-21 0-32l249-249c21-22 53 10 32 32z"
            : "M694 242l249 250c12 11 12 21 1 32L694 773c-5 5-10 7-16 7s-11-2-16-7c-11-11-11-21 0-32l210-210H68c-13 0-23-10-23-23s10-23 23-23h806L662 275c-21-22 11-54 32-33z"
        }
      />
    </svg>
  );
}

// the sizes react-dates set inline (calendar width, month padding, the 23px
// below the month, day size), so they apply also where its CSS is missing
export function calendarWidth(daySize: number, numberOfMonths: number) {
  return numberOfMonths * (7 * daySize + 27) + 19;
}

export function calendarStyles(
  daySize: number,
  numberOfMonths: number,
): DayPickerProps["styles"] {
  return {
    root: { width: calendarWidth(daySize, numberOfMonths) },
    months: { paddingLeft: 9 },
    month: { padding: "0 13px 23px" },
    weekday: { width: daySize },
    day: { width: daySize, height: daySize - 1 },
  };
}

export type RangeCalendarProps = RangeDates & {
  focusedInput: FocusedInput | null;
  onDatesChange(dates: RangeDates): void;
  onFocusChange(focusedInput: FocusedInput | null): void;
  onClose?(dates: RangeDates): void;
  minimumNights: number;
  keepOpenOnDateSelect: boolean;
  numberOfMonths: number;
  initialVisibleMonth: Date;
  daySize?: number;
  components?: DayPickerProps["components"];
  phrases?: {
    chooseAvailableStartDate(date: string): string;
    chooseAvailableEndDate(date: string): string;
  };
};

export function RangeCalendar({
  startDate,
  endDate,
  focusedInput,
  onDatesChange,
  onFocusChange,
  onClose,
  minimumNights,
  keepOpenOnDateSelect,
  numberOfMonths,
  initialVisibleMonth,
  daySize = 39,
  components,
  phrases,
}: RangeCalendarProps) {
  const [hoverDate, setHoverDate] = React.useState<Date | null>(null);
  const isBlocked = (day: Date) =>
    blocksMinimumNights(day, startDate, focusedInput, minimumNights);
  const isStart = (day: Date) =>
    startDate !== null && isSameDay(day, startDate);
  const isEnd = (day: Date) => endDate !== null && isSameDay(day, endDate);
  const chooseAvailable =
    (focusedInput === "startDate" && phrases?.chooseAvailableStartDate) ||
    (focusedInput === "endDate" && phrases?.chooseAvailableEndDate) ||
    chooseAvailablePhrase(focusedInput);

  return (
    <DayPicker
      locale={calendarLocale}
      numberOfMonths={numberOfMonths}
      defaultMonth={initialVisibleMonth}
      components={{ Chevron, ...components }}
      styles={calendarStyles(daySize, numberOfMonths)}
      disabled={isBlocked}
      modifiers={{
        selected: (day) => isStart(day) || isEnd(day),
        range_start: isStart,
        range_end: isEnd,
        range_middle: (day) =>
          startDate !== null &&
          endDate !== null &&
          differenceInCalendarDays(day, startDate) > 0 &&
          differenceInCalendarDays(endDate, day) > 0,
        hovered_span: (day) =>
          inHoveredSpan(
            day,
            hoverDate,
            { startDate, endDate, focusedInput, minimumNights },
            isBlocked,
          ),
      }}
      modifiersClassNames={{ hovered_span: "rdp-hovered_span" }}
      labels={{
        ...calendarLabels,
        labelDayButton: (day, modifiers) =>
          dayAriaLabel(day, modifiers, chooseAvailable),
      }}
      onDayMouseEnter={(day) => setHoverDate(day)}
      onDayMouseLeave={() => setHoverDate(null)}
      onDayClick={(day) => {
        const next = rangeClick(atNoon(day), {
          startDate,
          endDate,
          focusedInput,
          minimumNights,
          keepOpenOnDateSelect,
        });
        if (!next) return;
        const dates = { startDate: next.startDate, endDate: next.endDate };
        onDatesChange(dates);
        if (next.focusedInput !== undefined) onFocusChange(next.focusedInput);
        if (next.focusedInput === null) onClose?.(dates);
      }}
    />
  );
}
