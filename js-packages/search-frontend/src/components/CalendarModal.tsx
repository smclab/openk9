import React from "react";
import { css } from "styled-components/macro";
import {
  GenericResultItem,
  DetailRendererProps,
  SearchToken,
  SortField,
  AnalysisResponseEntry,
} from "./client";
import { useRenderers } from "./useRenderers";
import "overlayscrollbars/css/OverlayScrollbars.css";
import { DetailMemo } from "./Detail";
import { ModalDetail } from "./ModalDetail";
import { FiltersHorizontalMemo } from "./FiltersHorizontal";
import {
  Configuration,
  ConfigurationUpdateFunction,
} from "../embeddable/entry";
import { FilterSvg } from "../svgElement/FiltersSvg";
import { DeleteLogo } from "./DeleteLogo";
import { Search } from "./Search";
import { SelectionsState } from "./useSelections";
import { SearchDateRange } from "../embeddable/Main";
import { CalendarLogo } from "./CalendarLogo";
import { DataRangePicker } from "./DateRangePicker";
import { DateRangePicker } from "react-dates";
import { CalendarMobileSvg } from "../svgElement/CalendarMobileSvg";

export function CalendarMobile({
  onChange,
  calendarDate,
  isVisibleCalendar,
}: {
  onChange(value: SearchDateRange): void;
  calendarDate: SearchDateRange;
  isVisibleCalendar: boolean;
}) {
  const [startDate, setStartDate] = React.useState<any | null>(null);
  const [endDate, setEndDate] = React.useState<any | null>(null);
  const [focusedInput, setFocusedInput] = React.useState(null);
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
  const componet = (
    <React.Fragment>
      <div
        css={css`
          display: flex;
          justify-content: space-beetween;
          background: #fafafa;
        `}
      >
        <div
          className="openk9-filter-list-container-title box-title"
          css={css`
            padding: 0px 16px;
            width: 100%;
            background: #fafafa;
            padding-top: 20px;
            padding-bottom: 13px;
            display: flex;
          `}
        >
          <div
            className="openk9-filter-list-container-internal-title "
            css={css`
              display: flex;
              gap: 5px;
            `}
          >
            <span>
              <CalendarMobileSvg />
            </span>
            <span className="openk9-filters-list-title title">
              <h2
                css={css`
                  font-style: normal;
                  font-weight: 700;
                  font-size: 18px;
                  height: 18px;
                  line-height: 22px;
                  display: flex;
                  align-items: center;
                  color: #3f3f46;
                  margin: 0;
                `}
              >
                Calendario
              </h2>
            </span>
          </div>
        </div>
        <button
          css={css`
            color: var(--openk9-grey-stone-600);
            font-size: 10px;
            font-family: Nunito Sans;
            font-weight: 700;
            line-height: 12px;
            display: flex;
            align-items: center;
            gap: 9px;
            margin-right: 21px;
          `}
          onClick={() => {
            // if (setIsVisibleFilters) setIsVisibleFilters(false);
          }}
          style={{ backgroundColor: "#FAFAFA", border: "none" }}
        >
          Chiudi <DeleteLogo heightParam={8} widthParam={8} />
        </button>
      </div>
      <div style={{ width: "100%" }}>
        <DateRangePicker
          startDate={startDate}
          endDate={endDate}
          onDatesChange={handleDatesChange}
          focusedInput={focusedInput}
          onFocusChange={handleFocusChange}
          keepOpenOnDateSelect={true}
          startDateId="startDate"
          hideKeyboardShortcutsPanel={true}
          numberOfMonths={1}
          isOutsideRange={() => false}
          endDateId="endDate"
          openDirection="down"
        />
      </div>
      {/* <button
          css={css`
            color: var(--openk9-grey-stone-600);
            font-size: 10px;
            font-family: Nunito Sans;
            font-weight: 700;
            line-height: 12px;
            display: flex;
            align-items: center;
            gap: 9px;
            margin-right: 21px;
          `}
          onClick={() => {
            if (setIsVisibleCa) setIsVisibleFilters(false);
          }}
          style={{ backgroundColor: "#FAFAFA", border: "none" }}
        >
          Chiudi <DeleteLogo heightParam={8} widthParam={8} />
        </button> */}
    </React.Fragment>
  );
  if (!isVisibleCalendar) return null;
  document.body.style.overflow = "hidden";
  return <ModalDetail padding="0px" background="white" content={componet} />;
}
export const CalendarMobileMemo = React.memo(CalendarMobile);
