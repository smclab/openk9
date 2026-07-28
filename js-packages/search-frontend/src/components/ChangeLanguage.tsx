/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import React, { useMemo } from "react";
import Select, {
  components,
  SingleValueProps,
  StylesConfig,
} from "react-select";
import { css } from "styled-components";
import { GloboSvg } from "../svgElement/Globo";

export type LanguageItem = {
  createDate: any;
  modifiedDate: any;
  id: number;
  name: string;
  value: string;
};

type Option = {
  value: string;
  name: string;
  icon: JSX.Element;
};

export function ChangeLanguage({
  setChangeLanguage,
  background = "white",
  minHeight = "40px",
  color = "#1e1c21",
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

  // lingua = controllo secondario (pill bianco, bordo neutro) affiancato al
  // Login che è la CTA primaria (pieno rosso): stessa altezza e raggio.
  const ACCENT =
    "var(--openk9-embeddable-search--secondary-active-color, #c0272b)";
  const NEUTRAL_BORDER =
    "var(--openk9-embeddable-search--border-color, #ced4da)";
  const styles: StylesConfig<Option, false> = {
    control: (base, state) => ({
      ...base,
      minHeight,
      height: minHeight,
      borderRadius: 10,
      backgroundColor: background,
      border: `1px solid ${NEUTRAL_BORDER}`,
      boxShadow:
        state.isFocused || state.menuIsOpen
          ? "0 0 0 3px rgba(20, 20, 20, 0.06)"
          : "none",
      ":hover": {
        border: `1px solid ${ACCENT}`,
      },
    }),
    valueContainer: (base) => ({ ...base, paddingLeft: 12 }),
    menu: (base, state) => ({
      ...base,
      borderRadius: 12,
      overflow: "hidden",
      zIndex: state.selectProps.menuIsOpen ? 1000 : 1,
    }),
    option: (base, state) => ({
      ...base,
      backgroundColor: state.isSelected
        ? ACCENT
        : state.isFocused
        ? `color-mix(in srgb, ${ACCENT} 10%, #fff)`
        : "white",
      color: state.isSelected ? "white" : "#1e1c21",
      cursor: "pointer",
      ":active": {
        backgroundColor: `color-mix(in srgb, ${ACCENT} 18%, #fff)`,
      },
    }),
    indicatorSeparator: () => ({ display: "none" }),
    dropdownIndicator: (base) => ({ ...base, color, padding: "0 8px 0 0" }),
    singleValue: (base) => ({ ...base, color }),
    placeholder: (base) => ({ ...base, color }),
  };

  const SingleValue = (props: SingleValueProps<Option, false>) => (
    <components.SingleValue {...props}>
      <div
        css={css`
          display: flex;
          align-items: center;
          gap: 6px;
        `}
      >
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
