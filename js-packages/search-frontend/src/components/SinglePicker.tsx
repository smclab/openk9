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
// A calendar icon opening a single date calendar, with the markup, classes and
// behaviour of react-dates' SingleDatePicker (its input stays, hidden by the caller).
import React from "react";
import { DayPicker } from "react-day-picker";
import { format } from "date-fns";
import { atNoon, dayAriaLabel } from "./dateRange";
import {
  calendarLabels,
  calendarLocale,
  calendarStyles,
  calendarWidth,
  Chevron,
} from "./RangeCalendar";
import {
  DateInput,
  DISPLAY_FORMAT,
  focusFromGridToWidget,
  pickerPosition,
  useOutsideClick,
} from "./RangePicker";

export function SinglePicker({
  date,
  onDateChange,
  focused,
  onFocusChange,
  id,
  placeholder,
  readOnly,
  isOutsideRange,
  openDirection,
  iconAriaLabel,
}: {
  date: Date | null;
  onDateChange(date: Date | null): void;
  focused: boolean;
  onFocusChange(arg: { focused: boolean }): void;
  id: string;
  placeholder: string;
  readOnly: boolean;
  isOutsideRange(day: Date): boolean;
  openDirection: "up" | "down";
  iconAriaLabel: string;
}) {
  const rootRef = React.useRef<HTMLDivElement | null>(null);
  const pickerRef = React.useRef<HTMLDivElement | null>(null);
  const [isDayPickerFocused, setIsDayPickerFocused] = React.useState(false);

  const onOutsideClick = React.useCallback(() => {
    setIsDayPickerFocused(false);
    onFocusChange({ focused: false });
  }, [onFocusChange]);
  useOutsideClick(rootRef, focused, onOutsideClick);

  // with readOnly react-dates moved the focus into the calendar on opening
  React.useEffect(() => {
    if (!focused || !isDayPickerFocused) return;
    pickerRef.current
      ?.querySelector<HTMLElement>(".rdp-day_button[tabindex='0']")
      ?.focus();
  }, [focused, isDayPickerFocused]);

  return (
    <div ref={rootRef} className="SingleDatePicker">
      <div>
        <div className="SingleDatePickerInput SingleDatePickerInput__withBorder">
          <DateInput
            id={id}
            placeholder={placeholder}
            displayValue={date ? format(date, DISPLAY_FORMAT) : ""}
            focused={focused}
            showFang={focused}
            openDirection={openDirection}
            readOnly={readOnly}
          />
          {focused && (
            <div
              ref={pickerRef}
              className="SingleDatePicker_picker SingleDatePicker_picker__directionLeft"
              style={pickerPosition(openDirection)}
              onBlur={(event) => {
                if (focusFromGridToWidget(event, rootRef.current)) return;
                // onDayPickerFocusOut: focus moving to an element outside closes it
                const target =
                  event.relatedTarget === document.body
                    ? event.target
                    : event.relatedTarget || event.target;
                if (!pickerRef.current?.contains(target as Node)) {
                  onOutsideClick();
                }
              }}
            >
              <div
                className="DayPicker DayPicker__horizontal DayPicker__withBorder"
                style={{ width: calendarWidth(39, 1) }}
              >
                <div>
                  <div>
                    <DayPicker
                      mode="single"
                      locale={calendarLocale}
                      numberOfMonths={1}
                      defaultMonth={date || new Date()}
                      selected={date ?? undefined}
                      disabled={(day) => isOutsideRange(atNoon(day))}
                      components={{ Chevron }}
                      styles={calendarStyles(39, 1)}
                      labels={{
                        ...calendarLabels,
                        // the vertical picker gave react-dates phrases without
                        // chooseAvailableDate: the label of a free day was empty
                        labelDayButton: (day, modifiers) =>
                          dayAriaLabel(day, modifiers, () => ""),
                      }}
                      onSelect={(_, day) => {
                        onDateChange(atNoon(day));
                        onFocusChange({ focused: false });
                      }}
                    />
                  </div>
                </div>
              </div>
            </div>
          )}
          <button
            className="SingleDatePickerInput_calendarIcon"
            type="button"
            aria-label={iconAriaLabel}
            onClick={() => {
              setIsDayPickerFocused(readOnly);
              onFocusChange({ focused: true });
            }}
          >
            <svg
              className="SingleDatePickerInput_calendarIcon_svg"
              focusable="false"
              viewBox="0 0 1393.1 1500"
            >
              <path d="m107 1393h241v-241h-241zm295 0h268v-241h-268zm-295-295h241v-268h-241zm295 0h268v-268h-268zm-295-321h241v-241h-241zm616 616h268v-241h-268zm-321-616h268v-241h-268zm643 616h241v-241h-241zm-322-295h268v-268h-268zm-294-723v-241c0-7-3-14-8-19-6-5-12-8-19-8h-54c-7 0-13 3-19 8-5 5-8 12-8 19v241c0 7 3 14 8 19 6 5 12 8 19 8h54c7 0 13-3 19-8 5-5 8-12 8-19zm616 723h241v-268h-241zm-322-321h268v-241h-268zm322 0h241v-241h-241zm27-402v-241c0-7-3-14-8-19-6-5-12-8-19-8h-54c-7 0-13 3-19 8-5 5-8 12-8 19v241c0 7 3 14 8 19 6 5 12 8 19 8h54c7 0 13-3 19-8 5-5 8-12 8-19zm321-54v1072c0 29-11 54-32 75s-46 32-75 32h-1179c-29 0-54-11-75-32s-32-46-32-75v-1072c0-29 11-54 32-75s46-32 75-32h107v-80c0-37 13-68 40-95s57-39 94-39h54c37 0 68 13 95 39 26 26 39 58 39 95v80h321v-80c0-37 13-69 40-95 26-26 57-39 94-39h54c37 0 68 13 94 39s40 58 40 95v80h107c29 0 54 11 75 32s32 46 32 75z" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  );
}
