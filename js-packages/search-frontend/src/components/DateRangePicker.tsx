import React from "react";
import { DateRange, DefinedRange } from "react-date-range";
import "react-date-range/dist/styles.css"; // main css file
import "react-date-range/dist/theme/default.css"; // theme css file
import { css } from "styled-components/macro";
import { DateTime } from "luxon";
import { it } from "date-fns/locale";
import { SearchDateRange } from "../embeddable/Main";
import { useQuery } from "react-query";
import Select from "react-select";
import { useOpenK9Client } from "./client";

const DateRangeFix = DateRange as any;
const DefinedRangeFix = DefinedRange as any;

type DateRangePickerProps = {
  onClose(): void;
  onChange(value: SearchDateRange): void;
};
export function DateRangePicker({ onChange, onClose }: DateRangePickerProps) {
  const [value, setValue] = React.useState<SearchDateRange>({
    keywordKey: undefined,
    startDate: undefined,
    endDate: undefined,
  });
  const adaptedValue = [
    {
      key: "selection",
      startDate: value.startDate ?? null,
      endDate: value.endDate ?? null,
    },
  ];
  const adaptedOnChange = (item: any) => {
    setValue({
      keywordKey: value.keywordKey,
      startDate: item.selection.startDate,
      endDate: item.selection.endDate,
    });
  };
  const [selectedOption, setSelectedOption] = React.useState<{
    value: string;
    label: string;
  } | null>(null);
  const client = useOpenK9Client();
  const options = useQuery(["date-range-keywordkey-options", {}], async () => {
    return await client.getDateFilterFields();
  });
  return (
    <div>
      <Select
        value={selectedOption}
        onChange={(option) => {
          setSelectedOption(option);
          setValue({ ...value, keywordKey: option?.value });
        }}
        isLoading={options.isFetching}
        isSearchable={true}
        options={[
          { value: undefined as any, label: "Any" },
          ...(options.data?.map(({ id, field, label }) => ({
            value: field,
            label,
          })) ?? []),
        ]}
        theme={(theme) => ({
          ...theme,
          colors: {
            ...theme.colors,
            primary: "var(--openk9-embeddable-search--primary-color)",
            primary25:
              "var(--openk9-embeddable-search--secondary-background-color)",
          },
        })}
      />
      <div
        css={css`
          display: flex;
          border: 1px solid var(--openk9-embeddable-search--border-color);
          border-radius: 4px;
          overflow: hidden;
          margin-top: 16px;
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
          rangeColors={["var(--openk9-embeddable-search--primary-color)"]}
        />
      </div>
      <div
        css={css`
          display: flex;
          justify-content: flex-end;
          & > button {
            background-color: white;
            &:hover {
              background-color: var(--openk9-embeddable-search--active-color);
              color: white;
            }
            outline-color: var(--openk9-embeddable-search--active-color);
            border: 1px solid var(--openk9-embeddable-search--border-color);
            border-radius: 4px;
            padding: 8px 16px;
            margin-top: 8px;
            margin-left: 8px;
            font-family: inherit;
            font-size: inherit;
          }
        `}
      >
        <button
          onClick={() => {
            onChange({
              keywordKey: undefined,
              startDate: undefined,
              endDate: undefined,
            });
            onClose();
          }}
        >
          Non filtrare per data
        </button>
        <button
          disabled={!Boolean(value.startDate || value.endDate)}
          onClick={() => {
            onChange({
              keywordKey: value.keywordKey,
              startDate: value.startDate,
              endDate: value.endDate
                ? DateTime.fromJSDate(value.endDate).endOf("day").toJSDate()
                : undefined,
            });
            onClose();
          }}
        >
          Filtra
        </button>
      </div>
    </div>
  );
}

const staticRanges = [
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
