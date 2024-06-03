import React from "react";
import { css } from "styled-components/macro";
import { SortField } from "./client";

type Field = {
  field: string;
  id: number;
  isDefault: boolean;
  type: string;
  label: string;
  translationMap: {
    "label.en_US"?: string;
    "label.es_ES"?: string;
    "label.it_IT"?: string;
  };
};

export type Options = Field[];

type TypeSelectComponent = {
  selectOptions: Options;
  extraClass?: string;
  labelDefault?: string;
  language: string;
  handleChange?: (event: React.ChangeEvent<HTMLSelectElement>) => void;
  label?: string;
  classLabel?: string;
  selectedSort?: {
    field: string;
    type: string;
  };
};
function SelectComponent({
  selectOptions,
  extraClass = "",
  labelDefault = "Select Option",
  language,
  handleChange,
  classLabel = "visually-hidden",
  label = "Ordinamento",
  selectedSort,
}: TypeSelectComponent) {
  if (selectOptions.length === 0) return null;
  const keyLanguage = `label.${language}` as keyof Field["translationMap"];
  const cssStyles =
    classLabel === "visually-hidden"
      ? ` border: 0;
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
  white-space: nowrap;`
      : "";
  return (
    <>
      <label
        htmlFor="custom-select-sort"
        className={classLabel}
        css={cssStyles}
      >
        {label}
      </label>
      {selectedSort && (
        <select
          id="custom-select-sort"
          required
          className={`select-custom-openk9 ${extraClass}`}
          value={[selectedSort.field, selectedSort.type]}
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
            color: black;
            border: 2px solid red;
            &:hover {
              border: 2px solid red;
            }
            option {
              background: white;
              color: black;
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
          {selectOptions.map((option, index) => (
            <React.Fragment key={index}>
              {
                <option
                  value={[option.field, option.type]}
                  defaultChecked={option.isDefault}
                >
                  {option?.label}
                </option>
              }
            </React.Fragment>
          ))}
        </select>
      )}
    </>
  );
}

export default SelectComponent;
