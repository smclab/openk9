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
import React from "react";
import styled from "styled-components";

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

const Label = styled.label<{ visuallyHidden: boolean }>`
  ${({ visuallyHidden }) =>
    visuallyHidden &&
    `
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
`;

const Select = styled.select`
  padding: 7px 40px 7px 12px;
  border: 2px solid red;
  border-radius: 5px;
  background: white;
  box-shadow: 0 1px 3px -2px #9098a9;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  color: black;
  transition: all 150ms ease;
  @media (max-width: 768px) {
    width: 100%;
  }
  &:hover {
    border-color: red;
  }

  &:focus-visible {
    box-shadow: 0 0 0 0.125rem #fff, 0 0 0 0.25rem #ee4848;
    outline: 0;
  }

  option {
    background: white;
    color: black;
  }
`;

const SelectComponent: React.FC<TypeSelectComponent> = ({
  selectOptions,
  extraClass = "",
  labelDefault = "Select Option",
  language,
  handleChange,
  classLabel = "visually-hidden",
  label = "Ordinamento",
  selectedSort,
}) => {
  if (selectOptions.length === 0) return null;

  const keyLanguage = `label.${language}` as keyof Field["translationMap"];

  const sortedOptions = [...selectOptions].sort((a, b) =>
    a.isDefault === b.isDefault ? 0 : a.isDefault ? -1 : 1,
  );

  const selectedValue = selectedSort
    ? `${selectedSort.field}-${selectedSort.type}`
    : sortedOptions.find((opt) => opt.isDefault)?.field || "";

  return (
    <>
      <Label
        htmlFor="custom-select-sort"
        visuallyHidden={classLabel === "visually-hidden"}
      >
        {label}
      </Label>
      <Select
        id="custom-select-sort"
        required
        className={`select-custom-openk9 ${extraClass}`}
        value={selectedValue}
        onChange={handleChange}
      >
        {labelDefault && (
          <option value="" disabled>
            {labelDefault}
          </option>
        )}
        {sortedOptions.map((option) => (
          <option key={option.id} value={`${option.field}-${option.type}`}>
            {option.translationMap && option.translationMap[keyLanguage]
              ? option.translationMap[keyLanguage]
              : option?.label}
          </option>
        ))}
      </Select>
    </>
  );
};

export default SelectComponent;

