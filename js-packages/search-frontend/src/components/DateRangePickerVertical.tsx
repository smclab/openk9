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

export function DataRangePickerVertical({
  onChange,
  calendarDate,
  language,
  start,
  end,
}: {
  onChange(value: SearchDateRange): void;
  calendarDate: SearchDateRange;
  language: string;
  start?: any;
  end?: any;
}) {
  const [startDate, setStartDate] = React.useState<any | null>(null);
  const [focusedStartInput, setFocusedStartInput] = React.useState(false);
  const [endDate, setEndDate] = React.useState<any | null>(null);
  const [focusedEndInput, setFocusedEndInput] = React.useState(false);

  React.useEffect(() => {
    onChange({
      startDate: startDate?._d || undefined,
      endDate: endDate?._d || undefined,
      keywordKey: undefined,
    });
  }, [endDate]);

  const { t } = useTranslation();
  const languageCalendar = mappingNameLanguage(language);
  moment.locale(languageCalendar);

  return (
    <div
      className="DateRangePickerVertical-container"
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
        <SingleDatePicker
          date={start || startDate}
          numberOfMonths={1}
          onDateChange={(startDate) => setStartDate(startDate)}
          focused={focusedStartInput}
          onFocusChange={(focus) => setFocusedStartInput(focus.focused)}
          hideKeyboardShortcutsPanel
          id="startDate"
          showClearDate
          showDefaultInputIcon
          inputIconPosition="after"
          isOutsideRange={() => false}
          placeholder={t("start-day") || "Start day"}
          openDirection="up"
        />
      </div>
      <div className="DateRangePickerVertical-endDate-container">
        <p className="DateRangePickerVertical-date-title">
          Al ({t("gg/mm/aaaa")}):
        </p>
        <SingleDatePicker
          date={end || endDate}
          numberOfMonths={1}
          onDateChange={(endDate) => setEndDate(endDate)}
          focused={focusedEndInput}
          onFocusChange={(focus) => setFocusedEndInput(focus.focused)}
          hideKeyboardShortcutsPanel
          id="endDate"
          showClearDate
          showDefaultInputIcon
          inputIconPosition="after"
          disabled={startDate ? false : true}
          isOutsideRange={() => false}
          placeholder={t("end-day") || "End day"}
          openDirection="up"
        />
      </div>
    </div>
  );
}
