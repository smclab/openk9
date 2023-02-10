import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faLightbulb } from "@fortawesome/free-solid-svg-icons/faLightbulb";
import { faSearch } from "@fortawesome/free-solid-svg-icons/faSearch";
import { faSyncAlt } from "@fortawesome/free-solid-svg-icons/faSyncAlt";
import { faCalendar } from "@fortawesome/free-solid-svg-icons/faCalendar";
import { TokenSelect } from "../components/TokenSelect";
import { Tooltip } from "../components/Tooltip";
import {
  Configuration,
  ConfigurationUpdateFunction,
} from "../embeddable/entry";
import { useClickAway } from "./useClickAway";
import {
  AnalysisResponseEntry,
  AnalysisToken,
  GenericResultItem,
} from "./client";
import { SelectionsAction, SelectionsState } from "./useSelections";
import { DateRangePicker } from "./DateRangePicker";
import { SearchDateRange } from "../embeddable/Main";
import { Logo } from "./Logo";
import { CalendarLogo } from "./CalendarLogo";
import { Button } from "@mui/material";
import { DeleteLogo } from "./DeleteLogo";
import { SeparatorLogo } from "./SeparatorLogo";
import { DateTime } from "luxon";

type SearchProps = {
  configuration: Configuration;
  onConfigurationChange: ConfigurationUpdateFunction;
  onDetail(detail: GenericResultItem<unknown> | null): void;
  spans: Array<AnalysisResponseEntry>;
  selectionsState: SelectionsState;
  selectionsDispatch(action: SelectionsAction): void;
  showSyntax: boolean;
  dateRange: SearchDateRange;
  onDateRangeChange(dateRange: SearchDateRange): void;
};
export function Search({
  configuration,
  onConfigurationChange,
  spans,
  selectionsState,
  selectionsDispatch,
  onDetail,
  showSyntax,
  dateRange,
  onDateRangeChange,
}: SearchProps) {
  const autoSelect = configuration.searchAutoselect;
  const replaceText = configuration.searchReplaceText;

  const [openedDropdown, setOpenedDropdown] = React.useState<{
    textPosition: number;
    optionPosition: number;
  } | null>(null);

  const clickAwayRef = React.useRef<HTMLDivElement | null>(null);
  useClickAway([clickAwayRef], () => setOpenedDropdown(null));

  const inputRef = React.useRef<HTMLInputElement | null>(null);
  const [adjustedSelection, setAdjustedSelection] = React.useState<{
    selectionStart: number;
    selectionEnd: number;
  }>({ selectionStart: 0, selectionEnd: 0 });
  React.useLayoutEffect(() => {
    if (inputRef.current) {
      inputRef.current.selectionStart = adjustedSelection.selectionStart;
      inputRef.current.selectionEnd = adjustedSelection.selectionEnd;
    }
  }, [adjustedSelection]);

  const [isDatePickerOpen, setIsDatePickerOpen] = React.useState(false);
  const [valueSelected, setValueSelected] = React.useState<SearchDateRange>({
    keywordKey: undefined,
    startDate: undefined,
    endDate: undefined,
  });
  const [journey, setJourney] = React.useState();
  return (
    <div
      ref={clickAwayRef}
      className="openk9-embeddable-search--input-container"
      css={css`
        display: flex;
        align-items: center;
      `}
    >
      <FontAwesomeIcon
        icon={faSearch}
        style={{
          paddingLeft: "16px",
          opacity: 0.5,
          color: "--openk9-embeddable-search--secondary-text-color",
        }}
      />
      <div
        css={css`
          flex-grow: 1;
          position: relative;
          display: flex;
        `}
      >
        <div
          css={css`
            top: 0px;
            left: 0px;
            padding: var(--openk9-embeddable-search--input-padding);
            display: flex;
            position: absolute;
          `}
        >
          {showSyntax &&
            spans.map((span, index) => {
              const isOpen =
                openedDropdown !== null &&
                openedDropdown.textPosition > span.start &&
                openedDropdown.textPosition <= span.end;
              const optionIndex = openedDropdown?.optionPosition ?? null;
              const selection = selectionsState.selection.find(
                (selection) =>
                  selection.start === span.start && selection.end === span.end,
              );
              const selected = selection?.token ?? null;
              const onSelect = (token: AnalysisToken | null): void => {
                selectionsDispatch({
                  type: "set-selection",
                  replaceText,
                  selection: {
                    text: span.text,
                    start: span.start,
                    end: span.end,
                    token,
                    isAuto: false,
                  },
                });
                if (
                  inputRef.current?.selectionStart &&
                  inputRef.current?.selectionEnd
                ) {
                  setAdjustedSelection({
                    selectionStart: inputRef.current.selectionStart,
                    selectionEnd: inputRef.current.selectionEnd,
                  });
                }
                setOpenedDropdown(null);
              };
              const isAutoSelected = selection?.isAuto ?? false;
              const onOptionIndexChange = (optionIndex: number) => {
                setOpenedDropdown((openedDropdown) =>
                  openedDropdown
                    ? { ...openedDropdown, optionPosition: optionIndex }
                    : openedDropdown,
                );
              };
              return (
                <TokenSelect
                  key={index}
                  span={span}
                  isOpen={isOpen}
                  onOptionIndexChange={onOptionIndexChange}
                  optionIndex={optionIndex}
                  selected={selected}
                  onSelect={onSelect}
                  isAutoSlected={isAutoSelected}
                />
              );
            })}
        </div>
        <input
          ref={inputRef}
          placeholder="Cerca contenuti..."
          value={selectionsState.text}
          onChange={(event) => {
            selectionsDispatch({
              type: "set-text",
              text: event.currentTarget.value,
            });
            onDetail(null);
            setOpenedDropdown(null);
          }}
          css={css`
            position: relative;
            flex-grow: 1;
            border: none;
            outline: none;
            padding: var(--openk9-embeddable-search--input-padding);
            caret-color: black;
            font-size: inherit;
            font-family: inherit;
            background-color: inherit;
          `}
          spellCheck="false"
          onSelect={(event) => {
            if (
              (event.currentTarget.selectionDirection === "forward" ||
                event.currentTarget.selectionDirection === "none") &&
              event.currentTarget.selectionStart ===
                event.currentTarget.selectionEnd
            ) {
              setOpenedDropdown({
                textPosition: event.currentTarget.selectionStart as number,
                optionPosition: openedDropdown?.optionPosition ?? 0,
              });
            }
          }}
          onKeyDown={(event) => {
            const span =
              openedDropdown &&
              spans.find(
                (span) =>
                  openedDropdown.textPosition > span.start &&
                  openedDropdown.textPosition <= span.end,
              );
            const option =
              openedDropdown && span?.tokens[openedDropdown.optionPosition - 1];
            if (event.key === "ArrowDown") {
              event.preventDefault();
              if (openedDropdown && span) {
                setOpenedDropdown({
                  textPosition: openedDropdown.textPosition,
                  optionPosition:
                    openedDropdown.optionPosition < span.tokens.length
                      ? openedDropdown.optionPosition + 1
                      : 0,
                });
              }
            } else if (event.key === "ArrowUp") {
              event.preventDefault();
              if (openedDropdown && openedDropdown.optionPosition > 0) {
                setOpenedDropdown({
                  textPosition: openedDropdown.textPosition,
                  optionPosition: openedDropdown.optionPosition - 1,
                });
              }
            } else if (event.key === "Enter") {
              event.preventDefault();
              if (span) {
                selectionsDispatch({
                  type: "set-selection",
                  replaceText,
                  selection: {
                    text: span.text,
                    start: span.start,
                    end: span.end,
                    token: option ?? null,
                    isAuto: false,
                  },
                });
                if (replaceText) {
                  const text =
                    option &&
                    (option.tokenType === "ENTITY"
                      ? option.entityName
                      : option.value);
                  const cursorPosition = span.start + (text?.length ?? 0);
                  setAdjustedSelection({
                    selectionStart: cursorPosition,
                    selectionEnd: cursorPosition,
                  });
                } else if (
                  event.currentTarget.selectionStart &&
                  event.currentTarget.selectionEnd
                ) {
                  setAdjustedSelection({
                    selectionStart: event.currentTarget.selectionStart,
                    selectionEnd: event.currentTarget.selectionEnd,
                  });
                }
                setOpenedDropdown(null);
              }
            } else if (event.key === "Escape") {
              setOpenedDropdown(null);
            }
          }}
        ></input>
      </div>
      <div
        css={css`
          position: relative;
        `}
      >
        <div
          style={{
            paddingRight: "16px",
            display: "flex",
            flexDirection: "row",
            padding: "4px 8px",
            gap: "4px",
            alignItems: "center",
            marginRight: "16px",
          }}
        >
          {selectionsState.text && (
            <div>
              <span
                css={css`
                  cursor: pointer;
                `}
                onClick={() => {
                  selectionsDispatch({
                    type: "set-text",
                    text: "",
                  });
                }}
              >
                <DeleteLogo />
              </span>
              <span
                css={css`
                  margin-right: 18px;
                  margin-left: 23px;
                `}
              >
                <SeparatorLogo />
              </span>
            </div>
          )}
          <div
            css={css`
              cursor: pointer;
            `}
            onClick={() => {
              setIsDatePickerOpen(!isDatePickerOpen);
            }}
          >
            <CalendarLogo />
          </div>
          {journey ? (
            <CreateDeleteFilter
              journey={journey}
              onDateRangeChange={onDateRangeChange}
              setIsDatePickerOpen={setIsDatePickerOpen}
              setJourney={setJourney}
              isBuild={true}
              valueOfDate={valueSelected}
              setValueSelected={setValueSelected}
            />
          ) : (
            valueSelected.startDate && (
              <CreateDeleteFilter
                journey={journey || ""}
                onDateRangeChange={onDateRangeChange}
                setIsDatePickerOpen={setIsDatePickerOpen}
                setJourney={setJourney}
                isBuild={false}
                valueOfDate={valueSelected}
                setValueSelected={setValueSelected}
              />
            )
          )}
        </div>
        <div
          hidden={!isDatePickerOpen}
          css={css`
            position: absolute;
            right: 50%;
            border-radius: 4px;
            background-color: var(
              --openk9-embeddable-search--secondary-background-color
            );
            padding: 16px;
            border: 1px solid var(--openk9-embeddable-search--border-color);
            z-index: 2;
          `}
        >
          <DateRangePicker
            onChange={onDateRangeChange}
            onClose={() => setIsDatePickerOpen(false)}
            valueSelected={valueSelected}
            setValueSelected={setValueSelected}
            setJourney={setJourney}
          />
        </div>
      </div>
    </div>
  );
}

function CreateDeleteFilter({
  onDateRangeChange,
  setIsDatePickerOpen,
  setJourney,
  journey,
  isBuild,
  valueOfDate,
  setValueSelected,
}: {
  onDateRangeChange: any;
  setIsDatePickerOpen: any;
  setJourney: any;
  journey: string;
  isBuild: boolean;
  valueOfDate: { keywordKey: any; startDate: any; endDate: any };
  setValueSelected: any;
}) {
  let data;
  if (isBuild) {
    data = journey;
  } else {
    {
      data =
        "Dal " +
        valueOfDate.startDate?.toLocaleDateString("it-IT", {
          day: "2-digit",
          month: "2-digit",
          year: "numeric",
        }) +
        " al " +
        valueOfDate.endDate?.toLocaleDateString("it-IT", {
          day: "2-digit",
          month: "2-digit",
          year: "numeric",
        });
    }
  }
  return (
    <div
      css={css`
        cursor: pointer;
      `}
      style={{ boxSizing: "border-box" }}
      onClick={() => {
        onDateRangeChange({
          keywordKey: undefined,
          startDate: undefined,
          endDate: undefined,
        });
        setValueSelected({
          keywordKey: undefined,
          startDate: undefined,
          endDate: undefined,
        });
        setIsDatePickerOpen(false);
        setJourney("");
      }}
    >
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          padding: "4px 8px",
          gap: "4px",
          width: "100%",
          height: "20px",
          background: "#FFFFFF",
          border:
            "1px solid var(--openk9-embeddable-search--secondary-active-color)",
          borderRadius: "20px",
        }}
      >
        <p
          css={css`
            color: var(--openk9-embeddable-search--secondary-active-color);
            margin-bottom: 20px;
          `}
        >
          {data} x
        </p>
      </div>
    </div>
  );
}
