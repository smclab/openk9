import React from "react";
import SelectComponent from "./Select";
import { SortField } from "./client";

type TypeSortResultComponent = {
  selectOptions: Options;
  extraClass?: string;
  labelDefault?: string;
  setSortResult: (sortResultNew: SortField | undefined) => void;
  language: string;
  labelText?: string;
  classNameLabel: string | undefined;
  setSelectedSort: React.Dispatch<
    React.SetStateAction<{
      field: string;
      type: string;
    }>
  >;
  selectedSort: {
    field: string;
    type: string;
  };
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
  setSortResult,
  classNameLabel,
  selectedSort,
  setSelectedSort,
}: TypeSortResultComponent) {
  const handleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const value = event.target.value;
    const [label, type] = value.split(",");
    setSelectedSort({ field: label, type });
    if (label && (type === "asc" || type === "desc")) {
      setSortResult({
        [label]: {
          sort: type,
          missing: "_last",
        },
      });
    } else {
      setSortResult(undefined);
    }
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
      selectedSort={selectedSort}
    />
  );
}
