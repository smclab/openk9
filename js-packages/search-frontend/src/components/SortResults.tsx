import React from "react";
import SelectComponent from "./Select";
import { SortField } from "./client";

type TypeSortResultComponent = {
  selectOptions: Options;
  extraClass?: string;
  labelDefault?: string;
  setSortResult: (sortResultNew: SortField | undefined) => void;
  language: string;
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
  setSortResult,
}: TypeSortResultComponent) {
  const handleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const value = event.target.value;
    const [label, type] = value.split(",");
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

  return (
    <SelectComponent
      handleChange={handleChange}
      language={language}
      selectOptions={selectOptions}
      extraClass={extraClass}
      labelDefault={labelDefault}
    />
  );
}
