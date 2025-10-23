import { css } from "styled-components/macro";
import { UseQueryResult } from "react-query";
import React from "react";
import Select, { AriaOnFocus, components, StylesConfig } from "react-select";
import { useQuery } from "react-query";
import { useTranslation } from "react-i18next";

import { SortField, useOpenK9Client } from "../components/client";
import { useTranslation } from "react-i18next";
import Select, { AriaOnFocus, components } from "react-select";
import { setSortResultsType } from "./SortResults";
import "./SortResultList.css";

type OptionShape = {
  value: string | null;
  name: string;
  icon: React.ReactNode | null;
};

interface SortResultListProps {
  setSortResult: setSortResultsType;
  background?: string;
  minHeight?: string;
  color?: string;
  relevance?: string;
  HtmlString?: string;
  language?: string;
}

function SortResultList({
  setSortResult,
  relevance = "relevance",
  HtmlString = "",
  language,
}: SortResultListProps) {
  const { t } = useTranslation();
  const client = useOpenK9Client();

  const startValue: OptionShape = React.useMemo(
    () => ({ value: relevance, name: relevance, icon: null }),
    [relevance],
  );

  const [myValue, setMyValue] = React.useState<OptionShape>(startValue);

  const { data: labelSortData } = useQuery(
    ["date-label-sort-options", {}],
    async () => await client.getLabelSort(),
  );

  React.useEffect(() => {
    setMyValue({ value: relevance, name: relevance, icon: null });
  }, [relevance]);

  const sortOptions: OptionShape[] = React.useMemo(() => {
    const base: OptionShape[] = [startValue];

    if (labelSortData?.length) {
      for (const option of labelSortData) {
        base.push({
          value: JSON.stringify({ label: option.field, sort: "asc" }),
          name: `${option.label} ${t("asc")}`,
          icon: null,
        });
        base.push({
          value: JSON.stringify({ label: option.field, sort: "desc" }),
          name: `${option.label} ${t("desc")}`,
          icon: null,
        });
      }
    }
    return base;
  }, [labelSortData, startValue, t]);

  const SingleValue = (props: any) => (
    <components.SingleValue {...props}>
      <div style={{ display: "flex", alignItems: "center", gap: 5 }}>
        {props.children}
      </div>
    </components.SingleValue>
  );

  const handleChange = React.useCallback(
    (event: any) => {
      if (event.value === relevance) {
        setSortResult(undefined);
      } else {
        setSortResult({ field: event.value.label, type: event.value.sort });
      }
      setMyValue(event);
    },
    [relevance, setSortResult],
  );

  const customStyles: StylesConfig<OptionShape, false> = React.useMemo(
    () => ({
      control: (provided) => ({
        ...provided,
      }),
      menu: (provided, state) => ({
        ...provided,
        zIndex: state.selectProps.menuIsOpen ? 1000 : 1,
      }),
      option: (provided, state) => ({
        ...provided,
        backgroundColor: state.isFocused ? "#your-option-focus-color" : "white",
        color: "black",
        ":hover": {
          backgroundColor: state.isSelected ? "#d54949" : "#e836362e",
          cursor: "pointer",
        },
        ...(state.isSelected && {
          backgroundColor: "#d54949",
          color: "white",
        }),
      }),
      indicatorSeparator: () => ({
        display: "none",
      }),
    }),
    [],
  );

  const onFocus: AriaOnFocus<OptionShape> = ({ focused }) =>
    `${t("you-are-on")}${focused.name}`;

  return (
    <span className="openk9-container-sort-result-list-component">
      {!HtmlString && (
        <label
          className="visually-hidden"
          htmlFor="defaultSort"
          css={css`
            border: 0;
            padding: 0;
            margin: 0;
            position: absolute !important;
            height: 1px;
            width: 1px;
            overflow: hidden;
            clip: rect(1px, 1px, 1px, 1px);
            clip-path: inset(50%);
            white-space: nowrap;
          `}
        >
          Ordinamento
        </label>
      )}

      {relevance && (
        <Select
          tabIndex={0}
          inputId={HtmlString || "defaultSort"}
          aria-label=""
          aria-labelledby=""
          ariaLiveMessages={{ onFocus }}
          className="openk9-react-select-container"
          classNamePrefix="openk9-react-select"
          options={sortOptions}
          components={{ SingleValue }}
          onChange={handleChange as any}
          getOptionLabel={(e) => e.name}
          getOptionValue={(e) => String(e.value)}
          value={myValue}
          styles={customStyles}
        />
      )}
    </span>
  );
}

export const SortResultListMemo = React.memo(SortResultList);
