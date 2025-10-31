import { css } from "styled-components/macro";
import { UseQueryResult } from "react-query";
import React from "react";
import { useQuery } from "react-query";
import { SortField, useOpenK9Client } from "../components/client";
import { useTranslation } from "react-i18next";
import Select, { AriaOnFocus, components } from "react-select";
import { setSortResultsType } from "./SortResults";

function SortResultList({
  setSortResult,
  relevance = "relevance",
  HtmlString = "",
  language,
}: {
  setSortResult: setSortResultsType;
  background?: string;
  minHeight?: string;
  color?: string;
  relevance?: string;
  HtmlString?: string;
  language?: string;
}) {
  const { t } = useTranslation();
  const startValue = {
    value: relevance,
    name: relevance,
    icon: null,
  };
  const [myValue, setMyValue] = React.useState({
    value: relevance,
    name: relevance,
    icon: null,
  });
  const client = useOpenK9Client();
  const options = useQuery(["date-label-sort-options", {}], async () => {
    return await client.getLabelSort();
  });

  React.useEffect(() => {
    setMyValue({ value: relevance, name: relevance, icon: null });
  }, [relevance]);
  const sortOptions = [startValue];

  if (options.data?.length) {
    for (const option of options.data) {
      sortOptions?.push({
        value: JSON.stringify({
          label: option.field,
          sort: "asc",
        }),
        name: option.label + " " + t("asc"),
        icon: null,
      });
      sortOptions?.push({
        value: JSON.stringify({
          label: option.field,
          sort: "desc",
        }),
        name: option.label + " " + t("desc"),
        icon: null,
      });
    }
  }

  const SingleValue = (props: any) => (
    <components.SingleValue {...props}>
      <div style={{ display: "flex", alignItems: "center", gap: "5px" }}>
        {props.children}
      </div>
    </components.SingleValue>
  );

  const handleChange = (event: any) => {
    if (event.value === relevance || event.value === relevance) {
      setSortResult(undefined);
    } else {
      setSortResult({ field: event.value.label, type: event.value.sort });
    }
    setMyValue(event);
  };

  const customStyles = {
    control: (provided: any, state: any) => ({
      ...provided,
    }),
    menu: (provided: any, state: any) => ({
      ...provided,
      zIndex: state.selectProps.menuIsOpen ? "1000" : "1",
    }),
    option: (provided: any, state: any) => ({
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
      display: "none", // Nasconde la linea separatoria
    }),
  };

  const onFocus: AriaOnFocus<any> = ({ focused }) => {
    const msg = t("you-are-on") + focused.name;
    return msg;
  };

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
            clip: rect(
              1px 1px 1px 1px
            ); /* IE6, IE7 - a 0 height clip, off to the bottom right of the visible 1px box */
            clip: rect(
              1px,
              1px,
              1px,
              1px
            ); /*maybe deprecated but we need to support legacy browsers */
            clip-path: inset(50%);
            white-space: nowrap;
          `}
        >
          {"Ordinamento"}
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
          onChange={handleChange}
          getOptionLabel={(e) => e.name}
          getOptionValue={(e) => e.value}
          value={myValue}
          styles={customStyles}
        />
      )}
    </span>
  );
}
export const SortResultListMemo = React.memo(SortResultList);
