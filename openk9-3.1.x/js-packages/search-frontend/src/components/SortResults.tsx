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
import React, { useState, useEffect } from "react";
import SelectComponent from "./Select";

export type Options = Field[];
export type Field = {
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

export type setSortResultsType = (
  sortField:
    | {
        field: string;
        type: "asc" | "desc";
      }
    | undefined
    | null,
) => void;

export type TypeSortResultComponent = {
  selectOptions: Options;
  extraClass?: string;
  labelDefault?: string;
  language: string;
  labelText?: string;
  classNameLabel?: string;
  setSort: setSortResultsType;
  sort?: {
    sort: {
      field: string;
      type: string;
    };
    isSort: boolean;
  };
  useMockData?: boolean;
};

const mockOptions: Options = [
  {
    field: "name",
    id: 1,
    isDefault: true,
    type: "asc",
    label: "Name",
    translationMap: {
      "label.en_US": "Name",
      "label.it_IT": "Nome",
    },
  },
  {
    field: "price",
    id: 2,
    isDefault: false,
    type: "desc",
    label: "Price",
    translationMap: {
      "label.en_US": "Price",
      "label.it_IT": "Prezzo",
    },
  },
];

const SortResults: React.FC<TypeSortResultComponent> = ({
  selectOptions,
  extraClass = "",
  labelDefault = "Select Option",
  language,
  labelText,
  setSort,
  classNameLabel,
  sort,
  useMockData = false,
}) => {
  const [options, setOptions] = useState<Options>(
    useMockData ? mockOptions : selectOptions,
  );

  useEffect(() => {
    if (!useMockData) {
      setOptions(selectOptions);
    }
  }, [selectOptions, useMockData]);

  const handleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const value = event.target.value;
    const [field, type] = value.split("-");
    if (field && type) {
      setSort({ field, type: type as "asc" | "desc" });
    } else {
      setSort(null);
    }
  };

  const sortedOptions = [...options].sort(
    (a, b) => Number(b.isDefault) - Number(a.isDefault),
  );

  return (
    <SelectComponent
      handleChange={handleChange}
      language={language}
      selectOptions={sortedOptions}
      extraClass={extraClass}
      labelDefault={labelDefault}
      label={labelText}
      classLabel={classNameLabel}
      selectedSort={sort?.sort}
    />
  );
};

export default SortResults;

