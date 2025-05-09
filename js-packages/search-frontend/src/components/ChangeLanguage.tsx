import { css } from "styled-components/macro";
import { UseQueryResult } from "react-query";
import React from "react";
import { useQuery } from "react-query";
import { SortField, useOpenK9Client } from "../components/client";
import { useTranslation } from "react-i18next";
import { GloboSvg } from "../svgElement/Globo";
import Select, { components } from "react-select";
import { i18n } from "i18next";
import { remappingLanguage } from "../embeddable/Main";

export function ChangeLanguage({
  setChangeLanguage,
  background = "white",
  minHeight = "40px",
  color = "#7e7e7e",
  languages,
  activeLanguage,
  i18nElement,
}: {
  setChangeLanguage: (sortResultNew: string) => void;
  background?: string;
  minHeight?: string;
  color?: string;
  activeLanguage: string;
  i18nElement: i18n;
  languages:
    | {
        createDate: any;
        modifiedDate: any;
        id: number;
        name: string;
        value: "value";
      }[]
    | undefined;
}) {
  const filterLanguages = languages?.find(
    (language) => language.value === activeLanguage,
  );

  const startValue = filterLanguages
    ? {
        value: filterLanguages.value,
        name: filterLanguages.name,
        icon: <GloboSvg />,
      }
    : null;

  const handleChange = (e: any) => {
    i18nElement.changeLanguage(remappingLanguage({ language: e.value }));
    setChangeLanguage(e.value);
  };
  const customStyles = {
    control: (provided: any, state: any) => ({
      ...provided,
      borderRadius: "50px",
      backgroundColor: "white",
      border:
        !state.isFocused || !state.isHovered
          ? "1px solid white"
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

  const defaultValue = {
    value: "",
    name: "Select Language",
    icon: <GloboSvg />,
  };
  const languageElement = languages?.map((language) => ({
    value: language.value,
    name: language.name,
    icon: <GloboSvg />,
  }));
  languageElement?.unshift(defaultValue as any);

  const SingleValue = (props: any) => (
    <components.SingleValue {...props}>
      <div style={{ display: "flex", alignItems: "center", gap: "5px" }}>
        <GloboSvg /> {/* Icona SVG */}
        {remappingLanguageToBack({ language: activeLanguage })}
      </div>
    </components.SingleValue>
  );

  return (
    <span>
      <Select
        value={startValue}
        options={languageElement}
        components={{ SingleValue }}
        onChange={handleChange}
        getOptionLabel={(e) => e.name}
        getOptionValue={(e) => e.value}
        styles={customStyles}
      />
    </span>
  );
}

function remappingLanguageToBack({ language }: { language: string }) {
  switch (language) {
    case "it_IT":
      return "ITA";
    case "es_ES":
      return "ESP";
    case "en_US":
      return "GBR";
    case "de_DE":
      return "DEU";
    case "fr_FR":
      return "FRA";
    default:
      return "GBR";
  }
}
