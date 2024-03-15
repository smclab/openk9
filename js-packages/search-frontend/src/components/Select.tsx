import React from "react";
import { css } from "styled-components/macro";
import { SortField } from "./client";

type FieldLabel = {
  label?: string;
  translationMap?: {
    "label.en_US"?: string;
    "label.es_ES"?: string;
    "label.it_IT"?: string;
  };
};

type Field = {
  field: string;
  id: number;
  isDefault: boolean;
  hasAsc: boolean;
  hasDesc: {};
  labels: {
    default?: FieldLabel;
    asc?: FieldLabel;
    desc?: FieldLabel;
  };
};

export type Options = Field[];

type TypeSelectComponent = {
  selectOptions: Options;
  extraClass?: string;
  labelDefault?: string;
  setSortResult: (sortResultNew: SortField) => void;
  language: string;
};
function SelectComponent({
  selectOptions,
  extraClass = "",
  labelDefault = "Select Option",
  language,
  setSortResult,
}: TypeSelectComponent) {
  const keyLanguage = ("label." + language) as
    | "label.en_US"
    | "label.es_ES"
    | "label.it_IT";
  const handleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const value = event.target.value;
    //   setSortResult({
    //     value: {
    //       sort: "asc",
    //       missing: "_last",
    //     },
    //   });
  };
  return (
    <div
      className="container-select"
      style={{
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        fontFamily: "Roboto, sans-serif",
      }}
    >
      <div
        className="select"
        style={{ position: "relative", minWidth: "200px" }}
      >
        <select
          id="slct"
          required
          className={extraClass}
          onChange={handleChange}
          css={css`
            padding: 7px 40px 7px 12px;
            width: 100%;
            border: 1px solid #e8eaed;
            border-radius: 5px;
            background: white;
            box-shadow: 0 1px 3px -2px #9098a9;
            cursor: pointer;
            font-size: 16px;
            transition: all 150ms ease;
            font-weight: 600;
            color: gray;
            border: 2px solid gray;
            &:hover {
              border: 2px solid red;
            }
            option {
              background: white;
              color: gray;
            }
            option:hover {
              background: yellow; /* Cambia qui il colore quando si passa sopra con il mouse */
            }
            &:focus-visible {
              box-shadow: 0 0 0 0.125rem #fff, 0 0 0 0.25rem #ee4848;
              outline: 0;
            }
          `}
        >
          {labelDefault !== "" && (
            <option value="" disabled selected>
              {labelDefault}
            </option>
          )}
          {selectOptions.map((option) => (
            <>
              {option?.labels?.default?.translationMap?.[keyLanguage] && (
                <option
                  value={option.labels.default.label}
                  defaultChecked={option.isDefault}
                >
                  {option?.labels?.default?.translationMap?.[keyLanguage]}
                </option>
              )}
              {option.hasAsc && (
                <option
                  value={option.labels.asc?.label}
                  defaultChecked={option.isDefault}
                >
                  {option?.labels?.asc?.translationMap?.[keyLanguage] || ""}
                </option>
              )}
              {option.hasDesc && (
                <option
                  value={option.labels.desc?.label}
                  defaultChecked={option.isDefault}
                >
                  {option?.labels?.desc?.translationMap?.[keyLanguage] || ""}
                </option>
              )}
            </>
          ))}
        </select>
      </div>
    </div>
  );
}

export default SelectComponent;
