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
import React from "react";
import { SingleDatePicker } from "react-dates";
import { SearchDateRange } from "../embeddable/Main";
import moment from "moment";
import { useTranslation } from "react-i18next";
import { mappingNameLanguage } from "./CalendarModal";
import "moment/locale/de";
import "moment/locale/it";
import "moment/locale/es";
import "moment/locale/fr";
import { DateRangePickerPhrases } from "react-dates/lib/defaultPhrases";
import { css } from "styled-components";
export function DataRangePickerVertical({
  onChange,
  calendarDate,
  language,
  start,
  end,
  classTab,
  readOnly = false,
  translationLabel,
  isOpenFilter = false,
}: {
  onChange(value: SearchDateRange): void;
  calendarDate: SearchDateRange;
  language: string;
  start?: any;
  end?: any;
  classTab?: string;
  readOnly?: boolean;
  isOpenFilter?: boolean;
  translationLabel:
    | {
        labelStart?: string;
        labelEnd?: string;
        placeholderStart?: string;
        placeholderEnd?: string;
        errorFormatData?: string;
        errorSelectData?: string;
        labelContainerDateTitleStart?: string;
        labelContainerDateTitleEnd?: string;
      }
    | undefined;
}) {
  const languageCalendar = mappingNameLanguage(language);
  moment.locale(languageCalendar);
  const { t } = useTranslation();

  const [startDate, setStartDate] = React.useState<any | null>(null);
  const [focusedStartInput, setFocusedStartInput] = React.useState(false);
  const [endDate, setEndDate] = React.useState<any | null>(null);
  const [focusedEndInput, setFocusedEndInput] = React.useState(false);
  const [dataEnd, setDataEnd] = React.useState("");
  const [dataStart, setDataStart] = React.useState("");
  const [validationStart, setValidationStart] = React.useState("");
  const [validationEnd, setValidationEnd] = React.useState("");
  React.useEffect(() => {
    if (startDate) {
      startDate.set({ hour: 0, minute: 0, second: 0, millisecond: 0 });
    }
    if (endDate) {
      endDate.set({ hour: 23, minute: 59, second: 59, millisecond: 999 });
    }

    if (endDate) {
      setDataEnd(endDate.format("DD/MM/YYYY"));
      setValidationEnd("");
    } else {
      setDataEnd("");
    }

    if (startDate) {
      setDataStart(startDate.format("DD/MM/YYYY"));
      setValidationStart("");
    } else {
      setDataStart("");
    }
  }, [endDate, startDate]);

  React.useEffect(() => {
    const nextStart = calendarDate?.startDate
      ? moment(calendarDate.startDate)
      : null;
    const nextEnd = calendarDate?.endDate ? moment(calendarDate.endDate) : null;

    setStartDate(nextStart);
    setEndDate(nextEnd);

    setDataStart(nextStart ? nextStart.format("DD/MM/YYYY") : "");
    setDataEnd(nextEnd ? nextEnd.format("DD/MM/YYYY") : "");

    setValidationStart("");
    setValidationEnd("");
  }, [calendarDate?.startDate, calendarDate?.endDate]);

  const customPhrasesStart = {
    ...DateRangePickerPhrases,
    clearDates: t("remove-dates"),
    focusStartDate: startDate
      ? t("remove-data-start") || "remove start date"
      : t("open-calendar-start-date") || "open calendar start date",
  };
  const customPhrasesEndDate = {
    ...DateRangePickerPhrases,
    clearDates: t("remove-dates"),
    focusStartDate: endDate
      ? t("remove-data-end") || "remove end date"
      : t("open-calendar-end-date") || "open calendar end date",
  };

  function handleEndDateChange(event: React.ChangeEvent<HTMLInputElement>) {
    const inputValue = event.target.value;
    const dateObject = moment(inputValue, "DD/MM/YYYY", true);
    if (dateObject.isValid() && (!startDate || dateObject >= startDate)) {
      setEndDate(dateObject);
      setValidationEnd("");
    } else {
      setEndDate(null);
      setDataEnd("");
      if (inputValue !== "") {
        if (!dateObject.isValid()) {
          setValidationEnd(
            translationLabel?.errorFormatData ||
              t("invalid-date-format") ||
              "Invalid date format",
          );
        } else {
          setValidationEnd(
            t(translationLabel?.errorSelectData || "end-date-before-start") ||
              "End date cannot be earlier than start date",
          );
        }
      }
    }
  }

  function handleStartDateChange(event: React.ChangeEvent<HTMLInputElement>) {
    const inputValue = event.target.value;
    const dateObject = moment(inputValue, "DD/MM/YYYY", true);
    if (dateObject.isValid() && (!endDate || dateObject <= endDate)) {
      setStartDate(dateObject);
      setValidationStart("");
    } else {
      setStartDate(null);
      setDataStart("");
      if (inputValue !== "") {
        if (!dateObject.isValid()) {
          setValidationStart(
            translationLabel?.errorFormatData ||
              t("invalid-date-format") ||
              "Invalid date format",
          );
        } else {
          setValidationStart(
            translationLabel?.errorSelectData ||
              t("start-date-after-end") ||
              "Start date cannot be later than end date",
          );
        }
      }
    }
  }

  return (
    <div
      className={`DateRangePickerVertical-container openk9-class-tab-${classTab}`}
      css={css`
        display: flex;
        height: 100%;
        justify-content: center;
        flex-direction: column;
        gap: 10px;
      `}
    >
      <div className="DateRangePickerVertical-startDate-container">
        <p
          className="DateRangePickerVertical-date-title"
          css={css`
            font-size: 14px;
            letter-spacing: 0.2px;
            color: var(--openk9-embeddable-search--secondary-text-color);
            font-weight: 700;
          `}
        >
          {translationLabel?.labelContainerDateTitleStart || t("from-date")}
          <span className="openk9-start-data-after"> ({t("gg/mm/aaaa")}):</span>
        </p>
        <div
          className="openk9-container-input-start-date"
          css={css`
            display: flex;
            align-items: center;
            border: 1px solid var(--openk9-embeddable-search--border-color, #ced4da);
            border-radius: 8px;
            transition: border-color 0.15s ease;
            &:focus-within {
              border-color: var(--openk9-embeddable-search--primary-color, #80bdff);
            }
          `}
        >
          <label
            className="visually-hidden"
            css={css`
              border: 0;
              padding: 0;
              margin: 0;
              position: absolute !important;
              height: 1px;
              width: 1px;
              overflow: hidden;
              clip: rect(1px 1px 1px 1px);
              clip: rect(1px, 1px, 1px, 1px);
              clip-path: inset(50%);
              white-space: nowrap;
            `}
            htmlFor={"input-start-date"}
          >
            {translationLabel?.labelStart || t("start-day")}
          </label>
          <input
            type="text"
            id={"input-start-date"}
            placeholder={
              translationLabel?.placeholderStart ||
              t("start-day") ||
              "Data Inizio"
            }
            className="input-start-calendar"
            value={dataStart}
            onChange={(event) => {
              const expression = /^[\/0-9]*$/;
              const value = event.currentTarget.value;
              if (expression.test(event.currentTarget.value)) {
                setDataStart(value);
              }
            }}
            onBlur={handleStartDateChange}
            css={css`
              flex: 1;
              min-width: 0;
              border: none;
              background: transparent;
              padding: 10px 12px;
              font-size: 14px;
              outline: none;
              color: var(--openk9-embeddable-search--secondary-text-color, #495057);
              &::placeholder {
                color: #adb5bd;
              }
            `}
          />
          <div
            css={css`
              display: flex;
              align-items: center;
              flex-shrink: 0;
              margin-right: 6px;
            `}
          >
            <div className="openk9-calendar-button">
              <SingleDatePicker
                date={start || startDate}
                numberOfMonths={1}
                readOnly={readOnly}
                onDateChange={(d) => {
                  setStartDate(d);
                  onChange({
                    startDate: (d as any)?._d || undefined,
                    endDate: endDate?._d || undefined,
                    keywordKey: undefined,
                  });
                }}
                focused={focusedStartInput}
                onFocusChange={({ focused } = { focused: false }) =>
                  setFocusedStartInput(!!focused)
                }
                hideKeyboardShortcutsPanel
                id="startDate"
                showDefaultInputIcon
                inputIconPosition="after"
                isOutsideRange={(day) => {
                  return (
                    day.isAfter(moment().endOf("day")) ||
                    (endDate && day.isAfter(endDate))
                  );
                }}
                placeholder={
                  translationLabel?.placeholderStart ||
                  t("start-day") ||
                  "Start day"
                }
                openDirection="up"
                phrases={customPhrasesStart}
              />
            </div>
            {startDate && (
              <button
                type="button"
                aria-label={t("remove-dates") || "Remove date"}
                onClick={() => {
                  setStartDate(null);
                  setDataStart("");
                  onChange({
                    startDate: undefined,
                    endDate: endDate?._d || undefined,
                    keywordKey: undefined,
                  });
                }}
                css={css`
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  background: transparent;
                  border: none;
                  border-radius: 50%;
                  width: 26px;
                  height: 26px;
                  padding: 0;
                  cursor: pointer;
                  color: #adb5bd;
                  font-size: 14px;
                  line-height: 1;
                  transition: color 0.15s ease, background-color 0.15s ease;
                  &:hover {
                    color: #dc3545;
                    background-color: rgba(220, 53, 69, 0.08);
                  }
                `}
              >
                &#x2715;
              </button>
            )}
          </div>
        </div>
        {validationStart !== "" && (
          <p
            id="error-message"
            role="alert"
            css={css`
              color: red;
            `}
          >
            {validationStart}
          </p>
        )}
      </div>
      <div className="DateRangePickerVertical-endDate-container">
        <p
          className="DateRangePickerVertical-date-title"
          css={css`
            font-size: 14px;
            letter-spacing: 0.2px;
            color: var(--openk9-embeddable-search--secondary-text-color);
            font-weight: 700;
          `}
        >
          {translationLabel?.labelContainerDateTitleEnd || t("to-date")}
          <span className="openk9-end-calendar-after-label">
            ({t("gg/mm/aaaa")}):
          </span>
        </p>
        <div
          className="openk9-container-input-end-date"
          css={css`
            display: flex;
            align-items: center;
            border: 1px solid var(--openk9-embeddable-search--border-color, #ced4da);
            border-radius: 8px;
            transition: border-color 0.15s ease;
            &:focus-within {
              border-color: var(--openk9-embeddable-search--primary-color, #80bdff);
            }
          `}
        >
          <label
            className="visually-hidden"
            htmlFor={"input-end-date"}
            css={css`
              border: 0;
              padding: 0;
              margin: 0;
              position: absolute !important;
              height: 1px;
              width: 1px;
              overflow: hidden;
              clip: rect(1px 1px 1px 1px);
              clip: rect(1px, 1px, 1px, 1px);
              clip-path: inset(50%);
              white-space: nowrap;
            `}
          >
            {translationLabel?.labelEnd || t("end-day")}
          </label>
          <input
            id={"input-end-date"}
            type="text"
            placeholder={
              translationLabel?.placeholderEnd || t("end-day") || "Data Fine"
            }
            className="input-end-calendar"
            value={dataEnd}
            onChange={(event) => {
              const expression = /^[\/0-9]*$/;
              const value = event.currentTarget.value;
              if (expression.test(event.currentTarget.value)) {
                setDataEnd(value);
              }
            }}
            onBlur={handleEndDateChange}
            css={css`
              flex: 1;
              border: none;
              background: transparent;
              padding: 10px 12px;
              font-size: 14px;
              outline: none;
              color: var(--openk9-embeddable-search--secondary-text-color, #495057);
              &::placeholder {
                color: #adb5bd;
              }
            `}
          />
          <style>{`
            .DateInput {
              display: none;
            }
          `}</style>
          <div
            css={css`
              display: flex;
              align-items: center;
              flex-shrink: 0;
              margin-right: 6px;
            `}
          >
            <div className="openk9-calendar-button">
              <SingleDatePicker
                date={end || endDate}
                numberOfMonths={1}
                readOnly={readOnly}
                onDateChange={(d) => {
                  setEndDate(d);
                  onChange({
                    startDate: startDate?._d || undefined,
                    endDate: (d as any)?._d || undefined,
                    keywordKey: undefined,
                  });
                }}
                focused={focusedEndInput}
                onFocusChange={({ focused } = { focused: false }) =>
                  setFocusedEndInput(!!focused)
                }
                hideKeyboardShortcutsPanel
                id="endDate"
                showDefaultInputIcon
                inputIconPosition="after"
                isOutsideRange={(day) => {
                  return (
                    day.isAfter(moment().endOf("day")) ||
                    (startDate && startDate.isAfter(day))
                  );
                }}
                placeholder={
                  translationLabel?.placeholderEnd || t("end-day") || "End day"
                }
                openDirection="up"
                phrases={customPhrasesEndDate}
              />
            </div>
            {endDate && (
              <button
                type="button"
                aria-label={t("remove-dates") || "Remove date"}
                onClick={() => {
                  setEndDate(null);
                  setDataEnd("");
                  onChange({
                    startDate: startDate?._d || undefined,
                    endDate: undefined,
                    keywordKey: undefined,
                  });
                }}
                css={css`
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  background: transparent;
                  border: none;
                  border-radius: 50%;
                  width: 26px;
                  height: 26px;
                  padding: 0;
                  cursor: pointer;
                  color: #adb5bd;
                  font-size: 14px;
                  line-height: 1;
                  transition: color 0.15s ease, background-color 0.15s ease;
                  &:hover {
                    color: #dc3545;
                    background-color: rgba(220, 53, 69, 0.08);
                  }
                `}
              >
                &#x2715;
              </button>
            )}
          </div>
        </div>
        {validationEnd !== "" && (
          <p
            id="error-message"
            role="alert"
            css={css`
              color: red;
            `}
          >
            {validationEnd}
          </p>
        )}
      </div>
    </div>
  );
}

