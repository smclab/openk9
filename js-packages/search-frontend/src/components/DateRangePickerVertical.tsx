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
export function DataRangePickerVertical({
  onChange,
  calendarDate,
  language,
  start,
  end,
  classTab,
}: {
  onChange(value: SearchDateRange): void;
  calendarDate: SearchDateRange;
  language: string;
  start?: any;
  end?: any;
  classTab?: string;
}) {
  const languageCalendar = mappingNameLanguage(language);
  moment.locale(languageCalendar);
  const { t } = useTranslation();

  const [startDate, setStartDate] = React.useState<any | null>(null);
  const [focusedStartInput, setFocusedStartInput] = React.useState(false);
  const [endDate, setEndDate] = React.useState<any | null>(null);
  const [focusedEndInput, setFocusedEndInput] = React.useState(false);
  const [manageAccessibilityStart, setManageAccessibilityStart] =
    React.useState<boolean>(false);
  const [manageAccessibilityEnd, setManageAccessibilityEnd] =
    React.useState<boolean>(false);

  React.useEffect(() => {
    onChange({
      startDate: startDate?._d || undefined,
      endDate: endDate?._d || undefined,
      keywordKey: undefined,
    });
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

  return (
    <div
      className={`DateRangePickerVertical-container openk9-class-tab-${classTab}`}
      style={{
        display: "flex",
        height: "100%",
        justifyContent: "center",
        flexDirection: "column",
        padding: "15px 0 15px 15px",
      }}
    >
      <div className="DateRangePickerVertical-startDate-container">
        <p className="DateRangePickerVertical-date-title">
          Dal ({t("gg/mm/aaaa")}):
        </p>
        <div
          onClick={() => setManageAccessibilityStart(!manageAccessibilityStart)}
          onKeyDown={(e) =>
            e.key === "Tab" ? setManageAccessibilityStart(false) : null
          }
        >
          <SingleDatePicker
            date={start || startDate}
            numberOfMonths={1}
            readOnly={true}
            onDateChange={(startDate) => setStartDate(startDate)}
            focused={manageAccessibilityStart ? focusedStartInput : false}
            onFocusChange={(focus) => setFocusedStartInput(focus.focused)}
            hideKeyboardShortcutsPanel
            id="startDate"
            showClearDate
            showDefaultInputIcon
            inputIconPosition="after"
            isOutsideRange={(day) => {
              return (
                day.isAfter(moment().endOf("day")) || (endDate && day.isAfter(endDate))
              );
            }}
            placeholder={t("start-day") || "Start day"}
            openDirection="up"
            phrases={customPhrasesStart}
          />
        </div>
      </div>
      <div className="DateRangePickerVertical-endDate-container">
        <p className="DateRangePickerVertical-date-title">
          Al ({t("gg/mm/aaaa")}):
        </p>
        <div
          onClick={() => setManageAccessibilityEnd(!manageAccessibilityEnd)}
          onKeyDown={(e) =>
            e.key === "Tab" ? setManageAccessibilityEnd(false) : null
          }
        >
          <SingleDatePicker
            date={end || endDate}
            numberOfMonths={1}
            readOnly={true}
            onDateChange={(endDate) => setEndDate(endDate)}
            focused={manageAccessibilityEnd ? focusedEndInput : false}
            onFocusChange={(focus) => setFocusedEndInput(focus.focused)}
            hideKeyboardShortcutsPanel
            id="endDate"
            showClearDate
            showDefaultInputIcon
            inputIconPosition="after"
            isOutsideRange={(day) => {
              return (
                day.isAfter(moment().endOf("day")) || (startDate && startDate.isAfter(day))
              );
            }}
            placeholder={t("end-day") || "End day"}
            openDirection="up"
            phrases={customPhrasesEndDate}
          />
        </div>
      </div>
    </div>
  );
}
