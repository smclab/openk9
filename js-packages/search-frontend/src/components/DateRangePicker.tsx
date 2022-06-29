import React from "react";
import { DateRange, DefinedRange } from "react-date-range";
import "react-date-range/dist/styles.css"; // main css file
import "react-date-range/dist/theme/default.css"; // theme css file
import { css } from "styled-components/macro";
import { DateTime } from "luxon";
import { it } from "date-fns/locale";
import { SearchDateRange } from "../embeddable/Main";

const DateRangeFix = DateRange as any;
const DefinedRangeFix = DefinedRange as any;

type DateRangePickerProps = {
  value: SearchDateRange;
  onChange(value: SearchDateRange): void;
};
export function DateRangePicker({ value, onChange }: DateRangePickerProps) {
  const adaptedValue = [
    {
      key: "selection",
      startDate: value.startDate ?? null,
      endDate: value.endDate ?? null,
    },
  ];
  const adaptedOnChange = (item: any) => {
    onChange({
      keywordKey: value.keywordKey,
      startDate: item.selection.startDate,
      endDate: item.selection.endDate,
    });
  };
  return (
    <div>
      <div
        css={css`
          background-color: white;
          padding: 8px;
          display: flex;
        `}
      >
        <strong>keywordKey: </strong>
        <input
          value={value.keywordKey}
          onChange={(event) =>
            onChange({
              ...value,
              keywordKey: event.currentTarget.value || undefined,
            })
          }
          css={css`
            margin-left: 8px;
            flex: 1;
          `}
        />
      </div>
      <div
        css={css`
          display: flex;
        `}
      >
        <DefinedRangeFix
          locale={it}
          onChange={adaptedOnChange}
          ranges={adaptedValue}
          staticRanges={staticRanges}
        />
        <DateRangeFix
          locale={it}
          onChange={adaptedOnChange}
          ranges={adaptedValue}
          startDatePlaceholder=""
          endDatePlaceholder=""
        />
      </div>
    </div>
  );
}

const staticRanges = [
  {
    label: "Non filtrare per data",
    range: () => ({
      startDate: undefined,
      endDate: undefined,
    }),
    isSelected({ startDate, endDate }: any) {
      return startDate === undefined && endDate === undefined;
    },
  },
  {
    label: "Oggi",
    range: () => ({
      startDate: DateTime.now().startOf("day").toJSDate(),
      endDate: DateTime.now().endOf("day").toJSDate(),
    }),
    isSelected({ startDate, endDate }: any) {
      return (
        startDate === DateTime.now().startOf("day").toJSDate() &&
        endDate === DateTime.now().endOf("day").toJSDate()
      );
    },
  },
  {
    label: "Questa settimana",
    range: () => ({
      startDate: DateTime.now().startOf("week").toJSDate(),
      endDate: DateTime.now().endOf("week").toJSDate(),
    }),
    isSelected({ startDate, endDate }: any) {
      return (
        startDate === DateTime.now().startOf("week").toJSDate() &&
        endDate === DateTime.now().endOf("week").toJSDate()
      );
    },
  },
  {
    label: "Questo Mese",
    range: () => ({
      startDate: DateTime.now().startOf("month").toJSDate(),
      endDate: DateTime.now().endOf("month").toJSDate(),
    }),
    isSelected({ startDate, endDate }: any) {
      return (
        startDate === DateTime.now().startOf("month").toJSDate() &&
        endDate === DateTime.now().endOf("month").toJSDate()
      );
    },
  },
  {
    label: "Quest' Anno",
    range: () => ({
      startDate: DateTime.now().startOf("year").toJSDate(),
      endDate: DateTime.now().endOf("year").toJSDate(),
    }),
    isSelected({ startDate, endDate }: any) {
      return (
        startDate === DateTime.now().startOf("year").toJSDate() &&
        endDate === DateTime.now().endOf("year").toJSDate()
      );
    },
  },
];
