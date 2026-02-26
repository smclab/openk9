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
import { SearchDateRange } from "../embeddable/Main";
import { useTranslation } from "react-i18next";
import "react-dates/initialize";
import { DateRangePicker } from "react-dates";
import { CreateLabel } from "./Filters";
import moment from "moment";
import { DeleteLogo } from "./DeleteLogo";
import { CalendarLogo } from "./CalendarLogo";
import { css } from "styled-components";
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
      css={css`
        display: flex;
        gap: 5px;
        padding: 15px 20px;
        overflow: auto;
      `}
    >
      <CreateLabel
        padding="10px 6px"
        label={t("today")}
        action={() => {
          setStartDate(moment().startOf("day"));
          setEndDate(moment().endOf("day"));
        }}
      />

      <CreateLabel
        padding="10px 6px"
        label={t("this-week")}
        action={() => {
          setStartDate(moment().startOf("week"));
          setEndDate(moment().endOf("week"));
        }}
      />

      <CreateLabel
        padding="10px 6px"
        label={t("this-month")}
        action={() => {
          setStartDate(moment().startOf("month"));
          setEndDate(moment().endOf("month"));
        }}
      />

      <CreateLabel
        padding="10px 6px"
        label={t("this-year")}
        action={() => {
          setStartDate(moment().startOf("year"));
          setEndDate(moment().endOf("year"));
        }}
      />
      <div
        className="custom-calendar-info"
        css={css`
          margin-left: auto;
          width: fit-content;
          display: flex;
          gap: 10px;
        `}
      >
        <CreateLabel
          padding="10px 6px"
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
          padding="10px 6px"
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
          padding="10px 6px"
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
          css={css`
            display: flex;
            height: 100%;
            background: none;
            justify-content: center;
            flex-direction: column;
          `}
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
          id={"reset-filter-data-picker"}
          css={css`
            z-index: 2;
            background-color: inherit;
            cursor: pointer;
            border: none;
            display: flex;
            flex-direction: column;
            justify-content: center;
            color: black;
            height: 100%;
            background: none;
          `}
          onClick={() => {
            setFocusedInput(null);
            const inputElement = document.getElementById("endDate");
            if (inputElement instanceof HTMLInputElement) {
              inputElement.value = "";
            }
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

export function resetFilterCalendar() {
  const resetButton = document.getElementById("reset-filter-data-picker");
  if (resetButton) {
    resetButton.click();
  }
  const resetButtonVerticalStart = document.querySelectorAll(
    ".SingleDatePickerInput_clearDate.SingleDatePickerInput_clearDate_1.SingleDatePickerInput_clearDate__default.SingleDatePickerInput_clearDate__default_2",
  );
  if (resetButtonVerticalStart) {
    resetButtonVerticalStart.forEach((resetButton) => {
      if (resetButton instanceof HTMLButtonElement) {
        resetButton.click();
      }
    });
  }
}

