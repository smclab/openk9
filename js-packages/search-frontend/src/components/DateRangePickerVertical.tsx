import React from "react";
import { SingleDatePicker } from "react-dates";
import { SearchDateRange } from "../embeddable/Main";
import "./dateRangePickerVertical.css";
import moment from "moment";
import { useTranslation } from "react-i18next";
import { formatterLanguage, mappingNameLanguage } from "./CalendarModal";
import "moment/locale/de";
import "moment/locale/it";
import "moment/locale/es";
import "moment/locale/fr";
import { DateRangePickerPhrases } from "react-dates/lib/defaultPhrases";
import { css } from "styled-components/macro";
export function DataRangePickerVertical({
  onChange,
  calendarDate,
  language,
  start,
  end,
  classTab,
  readOnly = false,
  translationLabel,
}: {
  onChange(value: SearchDateRange): void;
  calendarDate: SearchDateRange;
  language: string;
  start?: any;
  end?: any;
  classTab?: string;
  readOnly?: boolean;
  translationLabel:
    | {
        labelStart?: string;
        labelEnd?: string;
        placeholderStart?: string;
        placeholderEnd?: string;
        errorFormatData?: string;
        errorSelectData?: string;
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
      endDate.set({ hour: 0, minute: 0, second: 0, millisecond: 0 });
    }
    onChange({
      startDate: startDate?._d || undefined,
      endDate: endDate?._d || undefined,
      keywordKey: undefined,
    });
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
            translationLabel?.errorFormatData || "Formato data non valido",
          );
        } else {
          setValidationEnd(
            translationLabel?.errorSelectData ||
              "La data di fine non può essere inferiore alla data di inizio",
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
            translationLabel?.errorFormatData || "Formato data non valido",
          );
        } else {
          setValidationStart(
            translationLabel?.errorSelectData ||
              "La data di inizio non può essere inferiore alla data di fine",
          );
        }
      }
    }
  }

  return (
    <div
      className={`DateRangePickerVertical-container openk9-class-tab-${classTab}`}
      style={{
        display: "flex",
        height: "100%",
        justifyContent: "center",
        flexDirection: "column",
        gap: "10px",
      }}
    >
      <div className="DateRangePickerVertical-startDate-container">
        <p className="DateRangePickerVertical-date-title">
          {t("from")} ({t("gg/mm/aaaa")}):
        </p>
        <div
          className="openk9-container-input-start-date"
          css={css`
            display: flex;
            border: 1px solid gray;
            border-radius: 8px;
            gap: 20px;
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
              clip: rect(
                1px 1px 1px 1px
              ); /* IE6, IE7 - a 0 height clip, off to the bottom right of the visible 1px box */
              clip: rect(
                1px,
                1px,
                1px,
                1px
              ); /*maybe deprecated but we need to support legacy browsers */
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
              width: 100%;
              border: transparent;
              background: transparent;
            `}
          />
          <div className="openk9-calendar-button" css={css``}>
            <SingleDatePicker
              date={start || startDate}
              numberOfMonths={1}
              readOnly={readOnly}
              onDateChange={setStartDate}
              focused={focusedStartInput}
              onFocusChange={(focus) => setFocusedStartInput(focus.focused)}
              hideKeyboardShortcutsPanel
              id="startDate"
              showClearDate
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
        </div>
      </div>
      {validationStart !== "" && (
        <p id="error-message" role="alert" style={{ color: "red" }}>
          {validationStart}
        </p>
      )}
      <div className="DateRangePickerVertical-endDate-container">
        <p
          className="DateRangePickerVertical-date-title"
          css={css`
            font-weight: 700;
          `}
        >
          Al ({t("gg/mm/aaaa")}):
        </p>
        <div
          className="openk9-container-input-end-date"
          css={css`
            display: flex;
            border: 1px solid gray;
            border-radius: 8px;
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
              clip: rect(
                1px 1px 1px 1px
              ); /* IE6, IE7 - a 0 height clip, off to the bottom right of the visible 1px box */
              clip: rect(
                1px,
                1px,
                1px,
                1px
              ); /*maybe deprecated but we need to support legacy browsers */
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
              width: 100%;
              border: transparent;
              background: transparent;
            `}
          />
          <style>{`
                  .DateInput  {
                     display: none; 
                   }
                   .SingleDatePickerInput_clearDate__default_2{
                      width:88%;
                    }
         `}</style>
          <div className="openk9-calendar-button" css={css``}>
            <SingleDatePicker
              date={end || endDate}
              numberOfMonths={1}
              readOnly={readOnly}
              onDateChange={setEndDate}
              focused={focusedEndInput}
              onFocusChange={(focus) => setFocusedEndInput(focus.focused)}
              hideKeyboardShortcutsPanel
              id="endDate"
              showClearDate
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
        </div>
        {validationEnd !== "" && (
          <p id="error-message" role="alert" style={{ color: "red" }}>
            {validationEnd}
          </p>
        )}
      </div>
    </div>
  );
}
