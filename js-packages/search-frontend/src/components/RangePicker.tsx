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
// Two date inputs and a range calendar popup, with the markup, classes and
// behaviour of react-dates' DateRangePicker.
import React from "react";
import { addDays, differenceInCalendarDays, format } from "date-fns";
import { atNoon, FocusedInput, parseDate, RangeDates } from "./dateRange";
import { calendarWidth, RangeCalendar } from "./RangeCalendar";

// moment "L" in English, the format of the react-dates inputs (see calendarLocale)
export const DISPLAY_FORMAT = "MM/dd/yyyy";

// react-dates' keyboard instructions, without the "?" shortcuts panel it hid
const FORWARD_INSTRUCTIONS =
  "Navigate forward to interact with the calendar and select a date.";
const BACKWARD_INSTRUCTIONS =
  "Navigate backward to interact with the calendar and select a date.";

// toMomentObject of react-dates: "L" or ISO, strict, at noon
export function parseInputDate(text: string) {
  const date = parseDate(text, [DISPLAY_FORMAT, "yyyy-MM-dd"]);
  return date && atNoon(date);
}

// the inline offset react-dates gave the popup and the fang, from its input height
export function pickerPosition(openDirection: "down" | "up") {
  return openDirection === "down"
    ? { top: 66, insetInlineStart: 0 }
    : { bottom: 66, insetInlineStart: 0 };
}

export function DateInput({
  id,
  placeholder,
  displayValue,
  focused,
  showFang,
  openDirection,
  readOnly = false,
  screenReaderMessage = FORWARD_INSTRUCTIONS,
  inputRef,
  onChange,
  onFocus,
  onKeyDown,
}: {
  id: string;
  placeholder: string;
  displayValue: string;
  focused: boolean;
  showFang: boolean;
  openDirection: "down" | "up";
  readOnly?: boolean;
  screenReaderMessage?: string;
  inputRef?: React.Ref<HTMLInputElement>;
  onChange?(text: string): void;
  onFocus?(): void;
  onKeyDown?(event: React.KeyboardEvent<HTMLInputElement>): void;
}) {
  const [typed, setTyped] = React.useState("");
  // react-dates drops the typed text as soon as there is a date to show
  if (typed && displayValue) setTyped("");
  const down = openDirection === "down";

  return (
    <div className="DateInput">
      <input
        ref={inputRef}
        className={
          focused
            ? "DateInput_input DateInput_input__focused"
            : "DateInput_input"
        }
        aria-label={placeholder}
        aria-describedby={`DateInput__screen-reader-message-${id}`}
        type="text"
        id={id}
        name={id}
        value={typed || displayValue}
        onChange={(event) => {
          setTyped(event.target.value);
          onChange?.(event.target.value);
        }}
        onKeyDown={(event) => {
          event.stopPropagation();
          // "?" opened the keyboard shortcuts panel of react-dates, which was hidden
          if (event.key === "?") event.preventDefault();
          onKeyDown?.(event);
        }}
        onFocus={onFocus}
        placeholder={placeholder}
        autoComplete="off"
        readOnly={readOnly}
      />
      {showFang && (
        <svg
          className="DateInput_fang"
          role="presentation"
          focusable="false"
          style={down ? { top: 55 } : { bottom: 55 }}
        >
          <path
            className="DateInput_fangShape"
            d={down ? "M0,10 20,10 10,0z" : "M0,0 20,0 10,10z"}
          />
          <path
            className="DateInput_fangStroke"
            d={down ? "M0,10 10,0 20,10" : "M0,0 10,10 20,0"}
          />
        </svg>
      )}
      <p
        className="DateInput_screenReaderMessage"
        id={`DateInput__screen-reader-message-${id}`}
      >
        {screenReaderMessage}
      </p>
    </div>
  );
}

// react-outside-click-handler: a click that starts and ends outside the element
export function useOutsideClick(
  ref: React.RefObject<HTMLElement | null>,
  enabled: boolean,
  onOutsideClick: () => void,
) {
  // react-outside-click-handler kept the mousedown across renders
  const callback = React.useRef(onOutsideClick);
  React.useEffect(() => {
    callback.current = onOutsideClick;
  });
  React.useEffect(() => {
    if (!enabled) return;
    let downOutside = false;
    const isOutside = (event: MouseEvent) =>
      !ref.current?.contains(event.target as Node);
    const onDown = (event: MouseEvent) => {
      downOutside = isOutside(event);
    };
    const onUp = (event: MouseEvent) => {
      if (downOutside && isOutside(event)) callback.current();
    };
    document.addEventListener("mousedown", onDown, true);
    document.addEventListener("mouseup", onUp, true);
    return () => {
      document.removeEventListener("mousedown", onDown, true);
      document.removeEventListener("mouseup", onUp, true);
    };
  }, [ref, enabled]);
}

// react-dates blurred days and arrows on mouseup: the focus then moving to the
// inputs (or the icon) of the same widget never closed the popup
export function focusFromGridToWidget(
  event: React.FocusEvent,
  widget: HTMLElement | null,
) {
  return (
    (event.target as Element).closest(
      ".rdp-day_button, .rdp-button_previous, .rdp-button_next",
    ) !== null && Boolean(widget?.contains(event.relatedTarget as Node))
  );
}

export type RangePickerProps = RangeDates & {
  focusedInput: FocusedInput | null;
  onDatesChange(dates: RangeDates): void;
  onFocusChange(focusedInput: FocusedInput | null): void;
  onClose?(dates: RangeDates): void;
  startDateId: string;
  endDateId: string;
  startDatePlaceholderText: string;
  endDatePlaceholderText: string;
  openDirection: "down" | "up";
  numberOfMonths?: number;
  keepOpenOnDateSelect?: boolean;
  noBorder?: boolean;
  renderCalendarInfo?(): React.ReactNode;
};

export function RangePicker({
  startDate,
  endDate,
  focusedInput,
  onDatesChange,
  onFocusChange,
  onClose,
  startDateId,
  endDateId,
  startDatePlaceholderText,
  endDatePlaceholderText,
  openDirection,
  numberOfMonths = 2,
  keepOpenOnDateSelect = false,
  noBorder = false,
  renderCalendarInfo,
}: RangePickerProps) {
  const rootRef = React.useRef<HTMLDivElement | null>(null);
  const pickerRef = React.useRef<HTMLDivElement | null>(null);
  const startRef = React.useRef<HTMLInputElement | null>(null);
  const endRef = React.useRef<HTMLInputElement | null>(null);
  // isDateRangePickerInputFocused and isDayPickerFocused of react-dates
  const [isInputFocused, setIsInputFocused] = React.useState(false);
  const [isDayPickerFocused, setIsDayPickerFocused] = React.useState(false);
  const isOpen = focusedInput !== null;

  const clearFocus = () => {
    onFocusChange(null);
    onClose?.({ startDate, endDate });
  };
  const onOutsideClick = React.useCallback(() => {
    setIsInputFocused(false);
    setIsDayPickerFocused(false);
    onFocusChange(null);
    onClose?.({ startDate, endDate });
  }, [onFocusChange, onClose, startDate, endDate]);
  useOutsideClick(rootRef, isOpen, onOutsideClick);

  const dayPickerBlur = () => {
    setIsInputFocused(true);
    setIsDayPickerFocused(false);
  };
  const dayPickerFocus = () => {
    if (!focusedInput) onFocusChange("startDate");
    setIsInputFocused(false);
    setIsDayPickerFocused(true);
  };

  // DateInput.componentDidUpdate: the focused input takes the DOM focus
  React.useEffect(() => {
    if (!isInputFocused || !focusedInput) return;
    (focusedInput === "startDate" ? startRef : endRef).current?.focus();
  }, [focusedInput, isInputFocused]);
  React.useEffect(() => {
    if (!isDayPickerFocused) return;
    pickerRef.current
      ?.querySelector<HTMLElement>(".rdp-day_button[tabindex='0']")
      ?.focus();
  }, [isDayPickerFocused]);

  const onStartDateChange = (text: string) => {
    const date = parseInputDate(text);
    if (date) {
      const endBeforeStart =
        endDate !== null &&
        differenceInCalendarDays(endDate, addDays(date, 1)) < 0;
      onDatesChange({
        startDate: date,
        endDate: endBeforeStart ? null : endDate,
      });
      onFocusChange("endDate");
    } else {
      onDatesChange({ startDate: null, endDate });
    }
  };
  const onEndDateChange = (text: string) => {
    const date = parseInputDate(text);
    if (
      date &&
      !(
        startDate !== null &&
        differenceInCalendarDays(date, addDays(startDate, 1)) < 0
      )
    ) {
      onDatesChange({ startDate, endDate: date });
      if (!keepOpenOnDateSelect) clearFocus();
    } else {
      onDatesChange({ startDate, endDate: null });
    }
  };
  const onInputFocus = (input: FocusedInput) => {
    dayPickerBlur();
    onFocusChange(input);
  };
  const onInputKeyDown = (
    input: FocusedInput,
    event: React.KeyboardEvent<HTMLInputElement>,
  ) => {
    if (event.key === "ArrowDown") dayPickerFocus();
    if (event.key === "Tab" && event.shiftKey && input === "startDate") {
      clearFocus();
    }
  };
  const width = calendarWidth(39, numberOfMonths);

  return (
    <div ref={rootRef} className="DateRangePicker">
      <div>
        <div
          className={
            noBorder
              ? "DateRangePickerInput"
              : "DateRangePickerInput DateRangePickerInput__withBorder"
          }
        >
          <DateInput
            id={startDateId}
            placeholder={startDatePlaceholderText}
            displayValue={startDate ? format(startDate, DISPLAY_FORMAT) : ""}
            focused={focusedInput === "startDate"}
            showFang={focusedInput === "startDate"}
            openDirection={openDirection}
            inputRef={startRef}
            onChange={onStartDateChange}
            onFocus={() => onInputFocus("startDate")}
            onKeyDown={(event) => onInputKeyDown("startDate", event)}
          />
          {isOpen && (
            <div
              ref={pickerRef}
              className="DateRangePicker_picker DateRangePicker_picker__directionLeft"
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
                style={{ width }}
              >
                <div>
                  <div
                    onKeyDown={(event) => {
                      if (event.key === "Escape") dayPickerBlur();
                    }}
                  >
                    <RangeCalendar
                      startDate={startDate}
                      endDate={endDate}
                      focusedInput={focusedInput}
                      onDatesChange={(dates) => {
                        onDatesChange(dates);
                        dayPickerBlur();
                      }}
                      onFocusChange={onFocusChange}
                      onClose={onClose}
                      minimumNights={1}
                      keepOpenOnDateSelect={keepOpenOnDateSelect}
                      numberOfMonths={numberOfMonths}
                      initialVisibleMonth={startDate || endDate || new Date()}
                    />
                  </div>
                  {renderCalendarInfo && <div>{renderCalendarInfo()}</div>}
                </div>
              </div>
            </div>
          )}
          <div
            className="DateRangePickerInput_arrow"
            aria-hidden="true"
            role="presentation"
          >
            <svg
              className="DateRangePickerInput_arrow_svg"
              focusable="false"
              viewBox="0 0 1000 1000"
            >
              <path d="M694 242l249 250c12 11 12 21 1 32L694 773c-5 5-10 7-16 7s-11-2-16-7c-11-11-11-21 0-32l210-210H68c-13 0-23-10-23-23s10-23 23-23h806L662 275c-21-22 11-54 32-33z" />
            </svg>
          </div>
          <DateInput
            id={endDateId}
            placeholder={endDatePlaceholderText}
            displayValue={endDate ? format(endDate, DISPLAY_FORMAT) : ""}
            focused={focusedInput === "endDate"}
            showFang={focusedInput === "endDate"}
            openDirection={openDirection}
            screenReaderMessage={BACKWARD_INSTRUCTIONS}
            inputRef={endRef}
            onChange={onEndDateChange}
            onFocus={() => onInputFocus("endDate")}
            onKeyDown={(event) => onInputKeyDown("endDate", event)}
          />
        </div>
      </div>
    </div>
  );
}
