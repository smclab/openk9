import { css } from "styled-components/macro";
import { UseQueryResult } from "react-query";
import React from "react";
import { useQuery } from "react-query";
import { SortField, useOpenK9Client } from "../components/client";
import { useTranslation } from "react-i18next";
import Select, { components } from "react-select";

export function SortResultList({
  setSortResult,
background = "white",
  minHeight = "40px",
  color = "#7e7e7e",
  relevance = "relevance",
}: {
  setSortResult: (sortResultNew: SortField) => void;
background?: string;
  minHeight?: string;
  color?: string;
  relevance?: string;
}) {
  const { t } = useTranslation();

  const startValue = {
    value: relevance,
    name: relevance === "relevance" ? t("relevance") : relevance,
    icon: null,
  };

  const client = useOpenK9Client();
  const options = useQuery(["date-label-sort-options", {}], async () => {
    return await client.getLabelSort();
  });

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
    if (event.value === "relevance") {
      setSortResult({});
    } else {
      setSortResult({
        [JSON.parse(event.value)?.label]: {
          sort: JSON.parse(event.value)?.sort,
          missing: "_last",
        },
      });
    }
  };

  const customStyles = {
    control: (provided: any, state: any) => ({
      ...provided,
      borderRadius: "50px",
      backgroundColor: "#FAFAFA",
      border:
        !state.isFocused || !state.isHovered
          ? "1px solid #FAFAFA"
          : "1px solid var(--openk9-embeddable-search--active-color)",
      boxShadow: "0 0 0 1px var(--openk9-embeddable-search--active-color)",
      ":hover": {
        border: "1px solid var(--openk9-embeddable-search--active-color)",
      },
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

  return (
    <span className="openk9-container-sort-result-list-component">
      <Select
        defaultValue={startValue}
        options={sortOptions}
        components={{ SingleValue }}
        onChange={handleChange}
        getOptionLabel={(e) => e.name}
        getOptionValue={(e) => e.value}
        styles={customStyles}
      />
    </span>
  );
}
