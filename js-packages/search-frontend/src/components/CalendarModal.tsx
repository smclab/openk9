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
import "moment/locale/it";
import "moment/locale/es";
import "moment/locale/fr";
import { useTranslation } from "react-i18next";
import "moment/locale/de";
import "moment/locale/it";
import "moment/locale/es";

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
  activeLanguage,
  isCLickReset,
  setIsCLickReset,
  language,
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
  activeLanguage?: string;
  isCLickReset: boolean;
  setIsCLickReset: React.Dispatch<React.SetStateAction<boolean>> | undefined;
  language: string;
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

  const { t } = useTranslation();
  React.useEffect(() => {
    onChange({
      startDate: undefined,
      endDate: undefined,
      keywordKey: undefined,
    });
    if (setIsCLickReset) setIsCLickReset(false);
  }, [isCLickReset]);
  const languageCalendar = mappingNameLanguage(language);
  moment.locale(languageCalendar);

  const componet = (
    <React.Fragment>
      <div
        css={css`
          display: flex;
          justify-content: space-beetween;
          background: #fafafa;
          padding-inline: 8px;
          align-items: center;
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
            align-items: center;
          `}
        >
          <div
            className="openk9-filter-list-container-internal-title "
            css={css`
              display: flex;
              gap: 5px;
              align-items: center;
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
                {t("filter-for-data")}
              </h2>
            </span>
          </div>
        </div>
        <button
          css={css`
            color: var(--openk9-grey-stone-600);
            font-size: 16px;
            font-family: Nunito Sans;
            font-weight: 700;
            line-height: 15px;
            display: flex;
            align-items: center;
            gap: 9px;
            margin-right: 21px;
            align-items: baseline;
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
          style={{ backgroundColor: "#FAFAFA", border: "none" }}
        >
          {t("close")} <DeleteLogo heightParam={8} widthParam={8} />
        </button>
      </div>
      <div
        className="openk9-button-for-search"
        css={css`
          display: flex;
          gap: 5px;
          overflow: scroll;
          margin-top: 10px;
          padding-inline: 25px;
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
          {t("today")}
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
          {t("this-week")}
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
          {t("this-month")}
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
          {t("this-year")}
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
          display: flex;
          gap: 2%;
          padding-inline: 28px;
          justify-content: center;
          margin-top: -20px;
        `}
      >
        <div>
          <input
            readOnly
            css={css`
              border-radius: 50px;
              height: 28px;
              text-align: center;
              opacity: 0.699999988079071;
              border: 1px solid
                var(--openk9-embeddable-search--secondary-active-color);
            `}
            value={
              moment(startDate).format("DD MMMM YYYY") === "Invalid date"
                ? t("start-day") || "Start day"
                : moment(startDate).format(formatterLanguage(language))
            }
          ></input>
        </div>
        <div>
          <input
            readOnly
            css={css`
              border-radius: 50px;
              height: 28px;
              text-align: center;
              opacity: 0.699999988079071;
              border: 1px solid
                var(--openk9-embeddable-search--secondary-active-color);
            `}
            value={
              moment(endDate).format("DD MMMM YYYY") === "Invalid date"
                ? t("end-day") || "End day"
                : moment(endDate).format(formatterLanguage(language))
            }
          ></input>
        </div>
      </div>
      <div
        css={css`
          border: 0.5px solid #d4d4d8;
          margin-top: 20px;
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
          <div>{t("don-t-filter-for-date")} </div>
          <div>
            <TrashSvg size="18px" />
          </div>
        </button>
        <button
          className="openk9-filter-horizontal-submit"
          aria-label={t("filter-for-data") || "filter for data"}
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
          <div>{t("filter-for-data")}</div>
          <div>
            <AddFiltersSvg size="21px" />
          </div>
        </button>
      </div>
      <style type="text/css">
        {`
        .DateInput {
          position: static !important;
          display: none !important;
        }
    `}
      </style>
    </React.Fragment>
  );
  if (!isVisibleCalendar) return null;
  document.body.style.overflow = "hidden";
  return <ModalDetail padding="0px" background="white" content={componet} />;
}
export function formatterLanguage(language: string) {
  switch (language) {
    case "it_IT":
      return "DD MMMM YYYY"; // Formato italiano
    case "es_ES":
      return "DD [de] MMMM [de] YYYY"; // Formato spagnolo
    case "fr_FR":
      return "DD MMMM YYYY"; // Formato francese
    case "de_DE":
      return "DD.MMMM.YYYY"; // Formato tedesco
    default:
      return "MMMM DD, YYYY"; // Formato predefinito (inglese)
  }
}
export function mappingNameLanguage(language: string) {
  switch (language) {
    case "it_IT":
      return "it";
    case "es_ES":
      return "es";
    case "fr_FR":
      return "fr";
    case "de_DE":
      return "de";
    default:
      return "en";
  }
}
export const CalendarMobileMemo = React.memo(CalendarMobile);
