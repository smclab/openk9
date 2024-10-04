import React from "react";
import { SortField, useOpenK9Client } from "../components/client";
import { useTranslation } from "react-i18next";
import Select, { AriaOnFocus, components } from "react-select";
import "./SortResultList.css";
import { setSortResultsType } from "./SortResults";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";

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

  const SingleValue = (props: any) => (
    <components.SingleValue {...props}>
      <div style={{ display: "flex", alignItems: "center", gap: "5px" }}>
        {props.children}
      </div>
    </components.SingleValue>
  );

  const CustomDropdownIndicator = (props: any) => {
    return (
      <components.DropdownIndicator {...props}>
        <div className="openk9-menu-open">
          {props.selectProps.menuIsOpen ? (
            <span className="" aria-hidden="true">
              <FontAwesomeIcon
                className="icon-search icon-search-filters"
                icon={faChevronUp}
                style={{ cursor: "pointer" }}
              />
            </span>
          ) : (
            <span
              className="fas fa-chevron-down down-icon"
              aria-hidden="true"
              style={{ cursor: "pointer" }}
            >
              <FontAwesomeIcon
                className="icon-search icon-search-filters"
                icon={faChevronDown}
                style={{ cursor: "pointer" }}
              />
            </span>
          )}
        </div>
      </components.DropdownIndicator>
    );
  };

  // TODO: `event` dovrà essere di tipo `{value: string | undefined, name: string | undefined, icon: string}`
  const handleChange = (event: any) => {
    const eventValue = event?.value && JSON.parse(event.value);
    if (eventValue) {
      setSortResult({
        field: eventValue.label.value,
        type: eventValue,
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
        components={{
          SingleValue,
          DropdownIndicator: CustomDropdownIndicator,
        }}
        options={sortOptions}
        onChange={handleChange}
        value={myValueMemo}
        styles={customStyles}
      />
    </span>
  );
}
export const SortResultListCustomMemo = React.memo(SortResultList);
