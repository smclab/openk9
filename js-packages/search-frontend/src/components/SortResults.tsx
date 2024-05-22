import React from "react";
import SelectComponent from "./Select";
import { SortField } from "./client";

type TypeSortResultComponent = {
  selectOptions: Options;
  extraClass?: string;
  labelDefault?: string;
  language: string;
  labelText?: string;
  classNameLabel: string | undefined;
  setSort: setSortResultsType;
  sort:
    | {
        sort: {
          field: string;
          type: string;
        };
        isSort: boolean;
      }
    | undefined;
};

export type Options = Field[];
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
export default function SortResults({
  selectOptions,
  extraClass = "",
  labelDefault = "Select Option",
  language,
  labelText,
  setSort,
  classNameLabel,
  sort,
}: TypeSortResultComponent) {
  const handleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const value = event.target.value;
    const [label, types] = value.split("-");

    setSort({ field: label, type: types as "asc" | "desc" });
  };

  const index = selectOptions.findIndex((obj) => obj.isDefault === true);

  if (index !== -1) {
    const defaultValueObj = selectOptions.splice(index, 1)[0];
    selectOptions.unshift(defaultValueObj);
  }

  return (
    <SelectComponent
      handleChange={handleChange}
      language={language}
      selectOptions={selectOptions}
      extraClass={extraClass}
      labelDefault={labelDefault}
      label={labelText}
      classLabel={classNameLabel}
      selectedSort={sort?.sort}
    />
  );
}

export type setSortResultsType = (
  sortField:
    | {
        field: string;
        type: "asc" | "desc";
      }
    | undefined
    | null,
) => void;
