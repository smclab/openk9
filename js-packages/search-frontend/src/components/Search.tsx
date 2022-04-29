import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faLightbulb,
  faSearch,
  faSyncAlt,
} from "@fortawesome/free-solid-svg-icons";
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
} from "@openk9/rest-api";
import { SelectionsAction, SelectionsState } from "./useSelections";

type SearchProps = {
  configuration: Configuration;
  onConfigurationChange: ConfigurationUpdateFunction;
  onDetail(detail: GenericResultItem<unknown> | null): void;
  spans: Array<AnalysisResponseEntry>;
  selectionsState: SelectionsState;
  selectionsDispatch(action: SelectionsAction): void;
  showSyntax: boolean;
};
export function Search({
  configuration,
  onConfigurationChange,
  spans,
  selectionsState,
  selectionsDispatch,
  onDetail,
  showSyntax
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
            color: ${showSyntax ? "transparent" : "inherit"};
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
                if (
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
      <Tooltip description="Sostituzione del testo quando si seleziona un suggerimento">
        <FontAwesomeIcon
          icon={faSyncAlt}
          style={{
            paddingRight: "16px",
            color: replaceText
              ? "var(--openk9-embeddable-search--primary-color)"
              : "var(--openk9-embeddable-search--secondary-text-color)",
            cursor: "pointer",
          }}
          onClick={() => {
            onConfigurationChange({ searchReplaceText: !replaceText });
          }}
        />
      </Tooltip>
      <Tooltip description="Seleziona automaticamente il suggerimento più pertinente">
        <FontAwesomeIcon
          icon={faLightbulb}
          style={{
            paddingRight: "16px",
            color: autoSelect
              ? "var(--openk9-embeddable-search--primary-color)"
              : "var(--openk9-embeddable-search--secondary-text-color)",
            cursor: "pointer",
          }}
          onClick={() => {
            onConfigurationChange({ searchAutoselect: !autoSelect });
          }}
        />
      </Tooltip>
    </div>
  );
}
