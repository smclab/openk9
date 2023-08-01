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
import { AddFiltersSvg } from "../svgElement/AddFiltersSvg";
import { TrashSvg } from "../svgElement/TrashSvg";
import moment from "moment";
// import "./CalendarModal.css";
export function CalendarMobile({
  onChange,
  calendarDate,
  isVisibleCalendar,
  setIsVisibleCalendar,
  startDate,
  endDate,
  focusedInput,
  setStartDate,
  setEndDate,
  setFocusedInput,
}: {
  onChange(value: SearchDateRange): void;
  calendarDate: SearchDateRange;
  isVisibleCalendar: boolean;
  setIsVisibleCalendar:
    | React.Dispatch<React.SetStateAction<boolean>>
    | undefined;
  startDate: any;
  endDate: any;
  focusedInput: any;
  setStartDate: any;
  setEndDate: any;
  setFocusedInput: any;
}) {
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
          padding-inline: 8px;
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
            if (setIsVisibleCalendar) setIsVisibleCalendar(false);
          }}
          style={{ backgroundColor: "#FAFAFA", border: "none" }}
        >
          Chiudi <DeleteLogo heightParam={8} widthParam={8} />
        </button>
      </div>
      <div
        className="openk9-button-for-search"
        css={css`
          display: flex;
          gap: 5px;
          overflow: scroll;
          margin-top: 10px;
          ::-webkit-scrollbar {
            display: none;
          }
        `}
      >
        <button
          css={css`
            background: #fafafa;
            padding: 8px 16px;
            font-size: 16px;
            font-style: normal;
            font-weight: 400;
            line-height: 22px;
            border: none;
            white-space: nowrap;
          `}
          onClick={() => {
            setStartDate(moment());
            setEndDate(moment());
          }}
        >
          Oggi
        </button>
        <button
          css={css`
            background: #fafafa;
            padding: 8px 16px;
            font-size: 16px;
            font-style: normal;
            font-weight: 400;
            line-height: 22px;
            border: none;
            white-space: nowrap;
          `}
          onClick={() => {
            setStartDate(moment().startOf("week"));
            setEndDate(moment().endOf("week"));
          }}
        >
          Questa settimana
        </button>
        <button
          css={css`
            background: #fafafa;
            padding: 8px 16px;
            font-size: 16px;
            font-style: normal;
            font-weight: 400;
            line-height: 22px;
            border: none;
            white-space: nowrap;
          `}
          onClick={() => {
            setStartDate(moment().startOf("month"));
            setEndDate(moment().endOf("month"));
          }}
        >
          Questo mese
        </button>
        <button
          css={css`
            background: #fafafa;
            padding: 8px 16px;
            font-size: 16px;
            font-style: normal;
            font-weight: 400;
            line-height: 22px;
            border: none;
            white-space: nowrap;
          `}
          onClick={() => {
            setStartDate(moment().startOf("year"));
            setEndDate(moment().endOf("year"));
          }}
        >
          Questo anno
        </button>
      </div>
      <div style={{ width: "100%", marginTop: "360px" }}>
        <DateRangePicker
          startDate={startDate}
          endDate={endDate}
          onDatesChange={handleDatesChange}
          focusedInput={focusedInput || "startDate"}
          onFocusChange={handleFocusChange}
          keepOpenOnDateSelect={true}
          startDateId="startDate"
          hideKeyboardShortcutsPanel={true}
          numberOfMonths={1}
          isOutsideRange={() => false}
          endDateId="endDate"
          openDirection="up"
          noBorder={true}
          verticalHeight={32}
        />
      </div>

      <div
        css={css`
          border: 0.5px solid #d4d4d8;
          margin-top: 10px;
        `}
      ></div>
      <div
        className="openk9-filter-horizontal-container-submit"
        css={css`
          display: flex;
          justify-content: flex-end;
          @media (max-width: 480px) {
            padding-inline: 20px;
            flex-direction: column;
          }
        `}
      >
        <button
          className="openk9-filter-horizontal-submit"
          aria-label="rimuovi filtri"
          css={css`
            font-size: smaller;
            height: 52px;
            padding: 8px 12px;
            white-space: nowrap;
            border: 1px solid #d6012e;
            background-color: #d6012e;
            border-radius: 5px;
            color: white;
            font-weight: 600;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 3px;
            @media (max-width: 480px) {
              background: white;
              border: 1px solid #d6012e;
              width: 100%;
              height: auto;
              margin-top: 20px;
              color: black;
              border-radius: 50px;
              display: flex;
              justify-content: center;
              color: var(--red-tones-500, #c0272b);
              text-align: center;
              font-size: 16px;
              font-style: normal;
              font-weight: 700;
              line-height: normal;
              align-items: center;
            }
          `}
          onClick={() => {
            if (setIsVisibleCalendar) setIsVisibleCalendar(false);
            onChange({
              startDate: undefined,
              endDate: undefined,
              keywordKey: undefined,
            });
            setStartDate(null);
            setEndDate(null);
          }}
        >
          <div>Non filtrare per data </div>
          <div>
            <TrashSvg size="18px" />
          </div>
        </button>
        <button
          className="openk9-filter-horizontal-submit"
          aria-label="applica filtri"
          css={css`
            font-size: smaller;
            height: 52px;
            padding: 8px 12px;
            white-space: nowrap;
            border: 1px solid #d6012e;
            background-color: #d6012e;
            border-radius: 5px;
            color: white;
            font-weight: 600;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 3px;
            @media (max-width: 480px) {
              background: #d6012e;
              border: 1px solid #d6012e;
              width: 100%;
              height: auto;
              margin-top: 20px;
              color: white;
              border-radius: 50px;
              display: flex;
              justify-content: center;
              text-align: center;
              font-size: 16px;
              font-style: normal;
              font-weight: 700;
              line-height: normal;
            }
          `}
          onClick={() => {
            if (setIsVisibleCalendar) setIsVisibleCalendar(false);
            onChange({
              startDate: startDate?._d || undefined,
              endDate: endDate?._d || undefined,
              keywordKey: undefined,
            });
          }}
        >
          <div>Mostra i risultati</div>
          <div>
            <AddFiltersSvg size="21px" />
          </div>
        </button>
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
