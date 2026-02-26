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
import { useTranslation } from "react-i18next";
import Select, { AriaOnFocus, components } from "react-select";
import { setSortResultsType } from "./SortResults";

function SortResultList({
  classTab,
  setSortResult,
  HtmlString = "",
  language,
  selectOptions,
  labelSelect,
}: {
  classTab?: string;
  setSortResult: setSortResultsType;
  background?: string;
  minHeight?: string;
  color?: string;
  HtmlString?: string;
  labelSelect?: string;
  language?: string;
  selectOptions: Array<{
    value: { value: string; sort: string };
    label: string;
    sort: string;
    isDefault: boolean;
    hasAscDesc: boolean;
  }>;
}) {
  const defaultOption = selectOptions.find((option) => option.isDefault);

  const [myValue, setMyValue] = React.useState({
    value: defaultOption?.value.value,
    name: defaultOption?.label,
    icon: "",
  });
  const myValueMemo = React.useMemo(() => myValue, [myValue]);
  const { t } = useTranslation();

  React.useEffect(() => {
    const findTabDefault = selectOptions.find((s) => s.isDefault);
    setSortResult({
      field: findTabDefault?.label || "",
      type: findTabDefault?.sort as "asc" | "desc",
    });
  }, [selectOptions]);

  const sortOptions = React.useMemo(() => {
    return selectOptions.flatMap((option) => {
      if (option.hasAscDesc) {
        return [
          {
            value: JSON.stringify({
              label: option.value,
              sort: "asc",
            }),
            name: option.label + " " + t("asc"),
            icon: "",
          },
          {
            value: JSON.stringify({
              label: option.value,
              sort: "desc",
            }),
            name: option.label + " " + t("desc"),
            icon: "",
          },
        ];
      } else {
        return {
          value: JSON.stringify({
            label: option.value,
            sort: "",
          }),
          name: option.label,
          icon: "",
        };
      }
    });
  }, [selectOptions, t]);

  // TODO: `event` dovrÃ  essere di tipo `{value: string | undefined, name: string | undefined, icon: string}`
  const handleChange = (event: any) => {
    const eventValue = event?.value && JSON.parse(event.value);

    if (eventValue) {
      setSortResult({
        field: eventValue.label.value,
        type: eventValue.label.sort,
      });
      setMyValue(event);
    }
  };

  const customStyles = {
    control: (provided: any, state: any) => ({
      ...provided,
    }),
    menu: (provided: any, state: any) => ({
      ...provided,
      zIndex: state.selectProps.menuIsOpen ? "1000" : "1",
    }),
    option: (provided: any, state: any) => ({
      ...provided,
      backgroundColor: state.isFocused ? "#your-option-focus-color" : "white",
      color: "black",
      ":hover": {
        backgroundColor: state.isSelected ? "#d54949" : "#e836362e",
        cursor: "pointer",
      },
      ...(state.isSelected && {
        backgroundColor: "#d54949",
        color: "white",
      }),
    }),
    indicatorSeparator: () => ({
      display: "none",
    }),
  };

  const onFocus: AriaOnFocus<any> = ({ focused }) => {
    const msg = t("you-are-on") + focused.name;
    return msg;
  };

  return (
    <span
      className={`openk9-container-sort-result-list-component openk9-class-tab-${classTab}`}
    >
      {!HtmlString && (
        <label className="openk9-label-sort" htmlFor="defaultSort">
          {labelSelect}
        </label>
      )}
      <Select
        getOptionLabel={(event) => event.name || ""}
        tabIndex={0}
        menuPlacement="auto"
        inputId={HtmlString || "defaultSort"}
        aria-label=""
        aria-labelledby=""
        ariaLiveMessages={{ onFocus }}
        className={`openk9-react-select-container SortResultListCustom-container`}
        classNamePrefix="openk9-react-select"
        options={sortOptions}
        onChange={handleChange}
        value={myValueMemo}
        styles={customStyles}
      />
    </span>
  );
}
export const SortResultListCustomMemo = React.memo(SortResultList);

