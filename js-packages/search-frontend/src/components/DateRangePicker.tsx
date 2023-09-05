import React from "react";
import { SearchDateRange } from "../embeddable/Main";
import "./dataRangePicker.css";
import { useTranslation } from "react-i18next";
import "react-dates/initialize";
import { DateRangePicker } from "react-dates";
import "react-dates/lib/css/_datepicker.css";
import { CreateLabel } from "./Filters";
import moment from "moment";
import { DeleteLogo } from "./DeleteLogo";
import { CalendarLogo } from "./CalendarLogo";
import { css } from "styled-components/macro";
import "moment/locale/de";
import "moment/locale/it";
import "moment/locale/es";
import { mappingNameLanguage } from "./CalendarModal";

export function DataRangePicker({
  onChange,
  calendarDate,
  start,
  end,
  language,
}: {
  onChange(value: SearchDateRange): void;
  calendarDate: SearchDateRange;
  start?: any;
  end?: any;
  language: string;
}) {
  const [startDate, setStartDate] = React.useState<any | null>(null);
  const [endDate, setEndDate] = React.useState<any | null>(null);
  const [focusedInput, setFocusedInput] = React.useState(null);
  const { t } = useTranslation();
  const languageCalendar = mappingNameLanguage(language);
  moment.locale(languageCalendar);

  const handleDatesChange = ({
    startDate,
    endDate,
  }: {
    startDate: any;
    endDate: any;
  }) => {
    setStartDate(startDate || undefined);
    setEndDate(endDate || undefined);
  };

  const handleFocusChange = (focusedInput: any) => {
    setFocusedInput(focusedInput);
  };
  const renderCalendarInfo = () => (
    <div
      className="custom-calendar-info"
      style={{
        display: "flex",
        gap: "20px",
        padding: "15px 20px",
        overflow: "auto",
      }}
    >
      <CreateLabel
        label={t("today")}
        action={() => {
          setStartDate(moment());
          setEndDate(moment());
        }}
      />
      <CreateLabel
        label={t("this-week")}
        action={() => {
          setStartDate(moment().startOf("week"));
          setEndDate(moment().endOf("week"));
        }}
      />
      <CreateLabel
        label={t("this-month")}
        action={() => {
          setStartDate(moment().startOf("month"));
          setEndDate(moment().endOf("month"));
        }}
      />
      <CreateLabel
        label={t("this-year")}
        action={() => {
          setStartDate(moment().startOf("year"));
          setEndDate(moment().endOf("year"));
        }}
      />
      <div
        className="custom-calendar-info"
        style={{
          marginLeft: "auto",
          width: "fit-content",
          display: "flex",
          gap: "10px",
        }}
      >
        <CreateLabel
          action={() => {
            onChange({
              startDate: undefined,
              endDate: undefined,
              keywordKey: undefined,
            });
            setStartDate(null);
            setEndDate(null);
          }}
          label={t("remove-filters")}
        />
        <CreateLabel
          action={() =>
            onChange({
              startDate: startDate?._d || undefined,
              endDate: endDate?._d || undefined,
              keywordKey: undefined,
            })
          }
          label={t("add-filters")}
        />
        <CreateLabel
          action={() => {
            document.getElementById("search-openk9")?.focus();
          }}
          label={t("close")}
        />
      </div>
    </div>
  );

  return (
    <div>
      <div
        css={css`
          display: flex;
          align-items: center;
          outline: var(--openk9-embeddable-search--border-color) solid 1px;
          border-radius: 50px;
          background: white;
          padding-inline: 10px;
          height: 50px;
          @media (max-width: 480px) {
            height: 40px;
          }
        `}
      >
        <div
          style={{
            display: "flex",
            height: "100%",
            background: "none",
            justifyContent: "center",
            flexDirection: "column",
          }}
        >
          <CalendarLogo size={"20px"} />
        </div>
        <DateRangePicker
          startDate={start || startDate}
          endDate={end || endDate}
          onDatesChange={handleDatesChange}
          focusedInput={focusedInput}
          onFocusChange={handleFocusChange}
          startDateId="startDate"
          endDateId="endDate"
          openDirection="down"
          hideKeyboardShortcutsPanel
          renderCalendarInfo={renderCalendarInfo}
          customInputIcon={null}
          keepOpenOnDateSelect={true}
          isOutsideRange={() => false}
          startDatePlaceholderText={t("start-day") || "Start day"}
          endDatePlaceholderText={t("end-day") || "Start day"}
          onClose={() => {
            if (calendarDate.startDate && calendarDate.endDate) {
              setStartDate(moment(calendarDate.startDate));
              setEndDate(moment(calendarDate.endDate));
            }
          }}
        />
        <button
          aria-label={t("remove-calendar-filters") || "remove calendar filters"}
          style={{
            zIndex: "2",
            backgroundColor: "inherit",
            cursor: "pointer",
            border: "none",
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            color: "black",
            height: "100%",
            background: "none",
          }}
          onClick={() => {
            onChange({
              keywordKey: undefined,
              startDate: undefined,
              endDate: undefined,
            });
            setStartDate(null);
            setEndDate(null);
          }}
        >
          <DeleteLogo heightParam={12} widthParam={12} />
        </button>
      </div>
    </div>
  );
}
