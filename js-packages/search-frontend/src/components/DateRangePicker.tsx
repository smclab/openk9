import React from "react";
import {
  DateRange,
  DefinedRange,
  defaultInputRanges,
  defaultStaticRanges,
} from "react-date-range";
import "react-date-range/dist/styles.css"; // main css file
import "react-date-range/dist/theme/default.css"; // theme css file
import { css } from "styled-components/macro";
import { DateTime } from "luxon";
import { enIN, it, es, fr } from "date-fns/locale";
import { SearchDateRange } from "../embeddable/Main";
import { useQuery } from "react-query";
import Select from "react-select";
import { useOpenK9Client } from "./client";
import "./dataRangePicker.css";
import { useTranslation } from "react-i18next";
import i18next from "i18next";
import ItLocale from "date-fns/locale/lt";
const DateRangeFix = DateRange as any;
const DefinedRangeFix = DefinedRange as any;

type DateRangePickerProps = {
  onClose(): void;
  onChange(value: SearchDateRange): void;
};
export function DateRangePicker({
  onChange,
  onClose,
  valueSelected,
  setValueSelected,
  setJourney,
}: DateRangePickerProps & {
  valueSelected: { keywordKey: any; startDate: any; endDate: any };
  setValueSelected: any;
  setJourney: any;
}) {
  const [value, setValue] = React.useState<SearchDateRange>({
    keywordKey: undefined,
    startDate: new Date(),
    endDate: new Date(),
  });
  const adaptedValue = [
    {
      key: "selection",
      startDate: value.startDate ?? null,
      endDate: value.endDate ?? null,
    },
  ];
  const [preJourney, preSetJourney] = React.useState("");
  const adaptedOnChange = (item: any) => {
    preSetJourney(item.selection.label);
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
  const { t } = useTranslation();
  const currentLanguage = i18next.language;
  const staticRanges = [
    {
      label: t("today"),
      range: () => ({
        startDate: DateTime.now().startOf("day").toJSDate(),
        endDate: DateTime.now().endOf("day").toJSDate(),
        label: t("today"),
      }),
      isSelected({ startDate, endDate }: any) {
        return (
          startDate === DateTime.now().startOf("day").toJSDate() &&
          endDate === DateTime.now().endOf("day").toJSDate()
        );
      },
    },
    {
      label: t("this-week"),
      range: () => ({
        startDate: DateTime.now().startOf("week").toJSDate(),
        endDate: DateTime.now().endOf("week").toJSDate(),
        label: t("this-week"),
      }),
      isSelected({ startDate, endDate }: any) {
        return (
          startDate === DateTime.now().startOf("week").toJSDate() &&
          endDate === DateTime.now().endOf("week").toJSDate()
        );
      },
    },
    {
      label: t("this-month"),
      range: () => ({
        startDate: DateTime.now().startOf("month").toJSDate(),
        endDate: DateTime.now().endOf("month").toJSDate(),
        label: t("this-month"),
      }),
      isSelected({ startDate, endDate }: any) {
        return (
          startDate === DateTime.now().startOf("month").toJSDate() &&
          endDate === DateTime.now().endOf("month").toJSDate()
        );
      },
    },
    {
      label: t("this-year"),
      range: () => ({
        startDate: DateTime.now().startOf("year").toJSDate(),
        endDate: DateTime.now().endOf("year").toJSDate(),
        label: t("this-year"),
      }),
      isSelected({ startDate, endDate }: any) {
        return (
          startDate === DateTime.now().startOf("year").toJSDate() &&
          endDate === DateTime.now().endOf("year").toJSDate()
        );
      },
    },
  ];
  const inputRangesLabels = {
    "days up to today": t("preview-days") || "days up to today",
    "days starting today": t("after-days") || "days starting today",
  };

  function translateRange(dictionary: any) {
    return (item: any) =>
      dictionary[item.label]
        ? { ...item, label: dictionary[item.label] }
        : item;
  }

  const ItInputRanges = defaultInputRanges.map(
    translateRange(inputRangesLabels),
  );
  return (
    <div>
      <Select
        defaultValue={{ value: "" as any, label: t("any") }}
        onChange={(option) => {
          setSelectedOption(option);
          setValue({ ...value, keywordKey: option?.value });
        }}
        isLoading={options.isFetching}
        isSearchable={true}
        options={[
          { value: "" as any, label: t("any") || "any" },
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
          @media (max-width: 480px) {
            flex-direction: column;
          }
        `}
      >
        <DefinedRangeFix
          locale={convert(currentLanguage)}
          onChange={adaptedOnChange}
          ranges={adaptedValue}
          staticRanges={staticRanges}
          inputRanges={ItInputRanges}
        />
        <DateRangeFix
          locale={convert(currentLanguage)}
          onChange={adaptedOnChange}
          ranges={adaptedValue}
          startDatePlaceholder=""
          endDatePlaceholder=""
          rangeColors={[
            "var(--openk9-embeddable-search--secondary-active-color)",
          ]}
        />
      </div>
      <div
        css={css`
          @media (max-width: 480px) {
            justify-content: flex-start;
          }
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
            setJourney();
            setValueSelected({
              keywordKey: undefined,
              startDate: undefined,
              endDate: undefined,
            });
            onClose();
          }}
        >
          {t("remove-data-filter")}
        </button>
        <button
          disabled={!Boolean(value.startDate || value.endDate)}
          onClick={() => {
            setJourney(preJourney);
            onChange({
              keywordKey: value.keywordKey,
              startDate: value.startDate,
              endDate: value.endDate
                ? DateTime.fromJSDate(value.endDate).endOf("day").toJSDate()
                : undefined,
            });
            setValueSelected({
              keywordKey: value.keywordKey,
              startDate: value.startDate,
              endDate: value.endDate
                ? DateTime.fromJSDate(value.endDate).endOf("day").toJSDate()
                : undefined,
            });
            onClose();
          }}
        >
          {t("filter")}
        </button>
      </div>
    </div>
  );
}

function convert(label: string) {
  switch (label) {
    case "it":
      return it;
    case "es":
      return es;
    case "en":
      return enIN;
    case "fr":
      return fr;
  }
}
