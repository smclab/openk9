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
};
function SelectComponent({
  selectOptions,
  extraClass = "",
  labelDefault = "Select Option",
  language,
  handleChange,
  classLabel = "visually-hidden",
  label = "Ordinamento",
}: TypeSelectComponent) {
  const keyLanguage = `label.${language}` as keyof Field["translationMap"];

  return (
    <div>
      <label htmlFor="custom-select-sort" className={classLabel}>
        {label}
      </label>
      <select
        id="custom-select-sort"
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
          <>
            {
              <option
                value={[option.field, option.type]}
                defaultChecked={option.isDefault}
                key={index}
              >
                {option?.translationMap?.[keyLanguage] || option?.label}
              </option>
            }
          </>
        ))}
      </select>
    </div>
  );
}

export default SelectComponent;
