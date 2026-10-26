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
// The rules of the react-dates pickers the date filters were built on, kept
// so that clicks, typed dates and day labels behave as before.
import {
  addDays,
  differenceInCalendarDays,
  format,
  isValid,
  parse,
} from "date-fns";

export type FocusedInput = "startDate" | "endDate";
export type RangeDates = { startDate: Date | null; endDate: Date | null };
type RangeState = RangeDates & { focusedInput: FocusedInput | null };

// react-dates built its days at noon
export function atNoon(day: Date) {
  const noon = new Date(day);
  noon.setHours(12, 0, 0, 0);
  return noon;
}

// strict like moment(text, formats, true): the date must format back to the same text
export function parseDate(text: string, formats: string[]) {
  for (const pattern of formats) {
    const date = parse(text, pattern, new Date());
    if (isValid(date) && format(date, pattern) === text) return date;
  }
  return null;
}

export function blocksMinimumNights(
  day: Date,
  startDate: Date | null,
  focusedInput: FocusedInput | null,
  minimumNights: number,
) {
  if (focusedInput !== "endDate" || !startDate) return false;
  const nights = differenceInCalendarDays(day, startDate);
  return nights >= 0 && nights < minimumNights;
}

// DayPickerRangeController.onDayClick: null when the day is blocked, focusedInput
// only when it changes (null also closes the picker)
export function rangeClick(
  day: Date,
  {
    startDate,
    endDate,
    focusedInput,
    minimumNights,
    keepOpenOnDateSelect,
  }: RangeState & { minimumNights: number; keepOpenOnDateSelect: boolean },
): (RangeDates & { focusedInput?: FocusedInput | null }) | null {
  if (blocksMinimumNights(day, startDate, focusedInput, minimumNights)) {
    return null;
  }
  if (focusedInput === "startDate") {
    const startAfterEnd =
      (endDate !== null &&
        differenceInCalendarDays(addDays(endDate, -minimumNights), day) < 0) ||
      (startDate !== null &&
        endDate !== null &&
        differenceInCalendarDays(startDate, endDate) > 0);
    return {
      startDate: day,
      endDate: startAfterEnd ? null : endDate,
      focusedInput: "endDate",
    };
  }
  if (focusedInput === "endDate") {
    if (!startDate)
      return { startDate, endDate: day, focusedInput: "startDate" };
    if (differenceInCalendarDays(day, startDate) >= minimumNights) {
      return keepOpenOnDateSelect
        ? { startDate, endDate: day }
        : { startDate, endDate: day, focusedInput: null };
    }
    return { startDate: day, endDate: null };
  }
  return { startDate, endDate };
}

// the days react-dates styled as hovered (getCalendarDaySettings.isHoveredSpan):
// "hovered-span", "after-hovered-start" and "before-hovered-end" of onDayMouseEnter
export function inHoveredSpan(
  day: Date,
  hoverDate: Date | null,
  {
    startDate,
    endDate,
    focusedInput,
    minimumNights,
  }: RangeState & { minimumNights: number },
  isBlocked: (day: Date) => boolean,
) {
  const between = (from: Date, to: Date) =>
    differenceInCalendarDays(day, from) >= 0 &&
    differenceInCalendarDays(to, day) >= 0;
  // getCalendarDaySettings: a selected day is never drawn as hovered
  const selected =
    (startDate && differenceInCalendarDays(day, startDate) === 0) ||
    (endDate && differenceInCalendarDays(day, endDate) === 0) ||
    (startDate && endDate && between(startDate, endDate));
  if (!hoverDate || !focusedInput || selected) return false;
  if (startDate && differenceInCalendarDays(hoverDate, startDate) === 0) {
    const after = differenceInCalendarDays(day, startDate);
    if (after > 0 && after <= minimumNights) return true;
  }
  if (endDate && differenceInCalendarDays(hoverDate, endDate) === 0) {
    const before = differenceInCalendarDays(endDate, day);
    if (before > 0 && before <= minimumNights) return true;
  }
  if (isBlocked(hoverDate)) return false;
  if (startDate && !endDate && focusedInput === "endDate") {
    return (
      differenceInCalendarDays(hoverDate, startDate) > 0 &&
      between(startDate, hoverDate)
    );
  }
  if (!startDate && endDate && focusedInput === "startDate") {
    return (
      differenceInCalendarDays(endDate, hoverDate) > 0 &&
      between(hoverDate, endDate)
    );
  }
  return false;
}

// moment "dddd, LL" in English, the date in the day labels of react-dates
export function ariaDate(day: Date) {
  return format(day, "EEEE, MMMM d, yyyy");
}

export function chooseAvailablePhrase(focusedInput: FocusedInput | null) {
  if (focusedInput === "startDate") {
    return (date: string) =>
      `Choose ${date} as your check-in date. It’s available.`;
  }
  if (focusedInput === "endDate") {
    return (date: string) =>
      `Choose ${date} as your check-out date. It’s available.`;
  }
  return (date: string) => date;
}

// the aria-label of a day in react-dates (getCalendarDaySettings)
export function dayAriaLabel(
  day: Date,
  modifiers: Record<string, boolean>,
  chooseAvailable: (date: string) => string,
) {
  const date = ariaDate(day);
  if (modifiers.range_start) return `Selected as start date. ${date}`;
  if (modifiers.range_end) return `Selected as end date. ${date}`;
  if (modifiers.selected || modifiers.range_middle) return `Selected. ${date}`;
  if (modifiers.disabled) return `Not available. ${date}`;
  return chooseAvailable(date);
}
