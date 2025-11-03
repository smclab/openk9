import { i18n } from "i18next";
import React from "react";
import Select, { components } from "react-select";
import { remappingLanguage } from "../embeddable/Main";
import { GloboSvg } from "../svgElement/Globo";

export function ChangeLanguage({
  setChangeLanguage,
  background = "white",
  minHeight = "40px",
  color = "#7e7e7e",
  languages,
  activeLanguage,
}: {
  setChangeLanguage: (lang: string) => void;
  background?: string;
  minHeight?: string;
  color?: string;
  activeLanguage: string;
  languages?: LanguageItem[];
}) {
  const options: Option[] = useMemo(
    () =>
      (languages ?? []).map((l) => ({
        value: l.value,
        name: l.name,
        icon: <GloboSvg />,
      })),
    [languages],
  );

  const startValue = useMemo<Option | null>(() => {
    const found = options.find((o) => o.value === activeLanguage);
    return found
      ? found
      : options[0]
      ? options[0]
      : { value: "", name: "Select Language", icon: <GloboSvg /> };
  }, [options, activeLanguage]);

  const handleChange = (opt: Option | null) => {
    if (!opt) return;
    setChangeLanguage(opt.value);
  };

  const styles: StylesConfig<Option, false> = {
    control: (base, state) => ({
      ...base,
      minHeight,
      borderRadius: 50,
      backgroundColor: background,
      border:
        state.isFocused || state.menuIsOpen
          ? "1px solid var(--openk9-embeddable-search--active-color)"
          : "1px solid white",
      boxShadow:
        state.isFocused || state.menuIsOpen
          ? "0 0 0 1px var(--openk9-embeddable-search--active-color)"
          : "none",
      ":hover": {
        border: "1px solid var(--openk9-embeddable-search--active-color)",
      },
    }),
    valueContainer: (base) => ({ ...base, paddingLeft: 12 }),
    menu: (base, state) => ({
      ...base,
      zIndex: state.selectProps.menuIsOpen ? 1000 : 1,
    }),
    option: (base, state) => ({
      ...base,
      backgroundColor: state.isFocused ? "#f4f4f4" : "white",
      color: state.isSelected ? "white" : "black",
      cursor: "pointer",
      ...(state.isSelected && { backgroundColor: "#d54949" }),
      ":hover": { backgroundColor: state.isSelected ? "#d54949" : "#e836362e" },
    }),
    indicatorSeparator: () => ({ display: "none" }),
    singleValue: (base) => ({ ...base, color }),
    placeholder: (base) => ({ ...base, color }),
  };

  const SingleValue = (props: SingleValueProps<Option, false>) => (
    <components.SingleValue {...props}>
      <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
        <GloboSvg />
        {remappingLanguageToBack({ language: activeLanguage })}
      </div>
    </components.SingleValue>
  );

  return (
    <span>
      <Select<Option, false>
        value={startValue}
        options={options}
        components={{ SingleValue }}
        onChange={handleChange}
        getOptionLabel={(o) => o.name}
        getOptionValue={(o) => o.value}
        styles={styles}
        isSearchable={false}
      />
    </span>
  );
}

function remappingLanguageToBack({ language }: { language: string }) {
  switch (language) {
    case "it_IT":
      return "ITA";
    case "pt_PT":
      return "PRT";
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
