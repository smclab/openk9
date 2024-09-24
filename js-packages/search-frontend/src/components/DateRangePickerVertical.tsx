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
}: {
  onChange(value: SearchDateRange): void;
  calendarDate: SearchDateRange;
  language: string;
  start?: any;
  end?: any;
  classTab?: string;
  readOnly?: boolean;
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
          setValidationEnd("Formato data non valido");
        } else {
          setValidationEnd(
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
          setValidationStart("Formato data non valido");
        } else {
          setValidationStart(
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
      }}
    >
      <div className="DateRangePickerVertical-startDate-container">
        <p className="DateRangePickerVertical-date-title">
          Dal ({t("gg/mm/aaaa")}):
        </p>
        <div
          css={css`
            display: flex;
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
            input per data inizio
          </label>
          <input
            type="text"
            id={"input-start-date"}
            placeholder="Data Inizio"
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
              border-top: 1px solid black;
              border-left: 1px solid black;
              border-bottom: 1px solid black;
              min-width: 130px;
              width: 100%;
              height: 42px;
            `}
          />
          <div
            className="openk9-calendar-button"
            css={css`
              border-top: 1px solid black;
              border-bottom: 1px solid black;
              border-left: none;
              border-right: 1px solid black;
            `}
          >
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
              placeholder={t("start-day") || "Start day"}
              openDirection="up"
              phrases={customPhrasesStart}
            />
            <style>{`
                  .DateInput  {
                     display: none; 
                   }
                   .SingleDatePickerInput_clearDate__default_2{
                      width:88%;
                    }
                      .SingleDatePickerInput__withBorder {
                        background: inherit;
                      }
           `}</style>
          </div>
        </div>
      </div>
      {validationStart !== "" && (
        <p id="error-message" role="alert" style={{ color: "red" }}>
          {validationStart}
        </p>
      )}
      <div className="DateRangePickerVertical-endDate-container">
        <p className="DateRangePickerVertical-date-title">
          Al ({t("gg/mm/aaaa")}):
        </p>
        <div
          css={css`
            display: flex;
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
            input per data fine
          </label>
          <input
            id={"input-end-date"}
            type="text"
            placeholder="Data Fine"
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
              border-top: 1px solid black;
              border-left: 1px solid black;
              border-bottom: 1px solid black;
              min-width: 130px;
              width: 100%;
              height: 42px;
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
          <div
            className="openk9-calendar-button"
            css={css`
              border-top: 1px solid black;
              border-bottom: 1px solid black;
              border-left: none;
              border-right: 1px solid black;
            `}
          >
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
              placeholder={t("end-day") || "End day"}
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
