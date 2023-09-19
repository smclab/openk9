import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSearch } from "@fortawesome/free-solid-svg-icons/faSearch";
import { TokenSelect } from "../components/TokenSelect";
import {
  Configuration,
  ConfigurationUpdateFunction,
} from "../embeddable/entry";
import { useClickAway } from "./useClickAway";
import {
  AnalysisResponseEntry,
  AnalysisToken,
  GenericResultItem,
  SearchToken,
  SortField,
} from "./client";
import { SelectionsAction, SelectionsState } from "./useSelections";
import { SearchDateRange } from "../embeddable/Main";
import { DeleteLogo } from "./DeleteLogo";
import { useTranslation } from "react-i18next";
import { ArrowLeftSvg } from "../svgElement/ArrowLeftSvg";
import { divide } from "lodash";

type SearchProps = {
  configuration: Configuration;
  onDetail(detail: GenericResultItem<unknown> | null): void;
  spans: Array<AnalysisResponseEntry>;
  selectionsState: SelectionsState;
  selectionsDispatch(action: SelectionsAction): void;
  showSyntax: boolean;
  isMobile: boolean;
  filtersSelect: SearchToken[];
  isVisibleFilters: boolean;
  mobileVersion?: boolean;
  btnSearch?: boolean;
  saveSearchQuery: React.Dispatch<React.SetStateAction<boolean>>;
  actionCloseMobileVersion?:
    | React.Dispatch<React.SetStateAction<boolean>>
    | undefined;
};
export function Search({
  configuration,
  spans,
  selectionsState,
  selectionsDispatch,
  onDetail,
  showSyntax,
  isMobile,
  mobileVersion = false,
  btnSearch = false,
  actionCloseMobileVersion,
  saveSearchQuery,
}: SearchProps) {
  const autoSelect = configuration.searchAutoselect;
  const replaceText = configuration.searchReplaceText;
  const [openedDropdown, setOpenedDropdown] = React.useState<{
    textPosition: number;
    optionPosition: number;
  } | null>(null);

  const clickAwayRef = React.useRef<HTMLDivElement | null>(null);
  useClickAway([clickAwayRef], () => setOpenedDropdown(null));

  const [textBtn, setTextBtn] = React.useState("");
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
  const { t } = useTranslation();
  return (
    <React.Fragment>
      <div
        className="openk9--search-container"
        css={css`
          margin-top: 12px;
          display: flex;
          align-items: center;
          gap: 10px;
          width: 100%;
          @media (max-width: 480px) {
            flex-direction: column;
            margin-top: 15px;
          }
          .openk9-focusable:has(input:focus) {
            border: 1px solid #c22525;
          }
        `}
      >
        <div
          ref={clickAwayRef}
          className="openk9-embeddable-search--input-container openk9-focusable"
          css={css`
            display: flex;
            align-items: center;
            border-radius: 40px;
            width: 100%;
            max-height: 50px;
            @media (max-width: 480px) {
              width: 100%;
              max-height: 40px;
            }
          `}
        >
          <FontAwesomeIcon
            className="openk9--search-icon"
            icon={faSearch}
            style={{
              paddingLeft: "16px",
              opacity: 0.5,
              color: "var(--openk9-embeddable-search--secondary-text-color)",
            }}
          />
          <div
            className="openk9--search-container-show-syntax"
            css={css`
              flex-grow: 1;
              position: relative;
              display: flex;
            `}
          >
            <div
              className="openk9--search-show-syntax"
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
                      selection.start === span.start &&
                      selection.end === span.end,
                  );
                  const selected = selection?.token ?? null;
                  const onSelect = (token: AnalysisToken | null): void => {
                    console.log(token, span.text, span.start, span.end);

                    selectionsDispatch({
                      type: "set-selection",
                      replaceText,
                      selection: {
                        text: span.text,
                        textOnChange: span.text,
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
                  const onSelectText = (token: AnalysisToken | null): void => {
                    if (token)
                      selectionsDispatch({
                        type: "set-text",
                        text: token.value,
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
                      onSelectText={onSelectText}
                      isAutoSlected={isAutoSelected}
                      setOpenedDropdown={setOpenedDropdown}
                      selectionsDispatch={selectionsDispatch}
                      saveSearchQuery={saveSearchQuery}
                    />
                  );
                })}
            </div>
            <label htmlFor="search-openk9" className="visually-hidden">
              Search
            </label>
            <input
              className="openk9--input-search"
              autoComplete="off"
              ref={inputRef}
              id="search-openk9"
              aria-label={
                t(
                  "insert-text-to-set-the-value-or-use-up-and-down-arrow-keys-to-navigate-the-suggestion-box",
                ) ||
                "insert text to set the value or use up and down arrow keys to navigate the suggestion box"
              }
              type="text"
              placeholder={t("search") || "search..."}
              value={btnSearch ? textBtn : selectionsState.text}
              onChange={(event) => {
                if (!btnSearch) {
                  selectionsDispatch({
                    type: "set-text",
                    text: event.currentTarget.value,
                    textOnchange: event.currentTarget.value,
                  });
                  onDetail(null);
                  setOpenedDropdown(null);
                } else {
                  setTextBtn(event.currentTarget.value);
                  selectionsDispatch({
                    type: "set-text",
                    textOnchange: event.currentTarget.value,
                  });
                }
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
                color: ${"black"};
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
                  openedDropdown &&
                  span?.tokens[openedDropdown.optionPosition - 1];
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
                        textOnChange: span.text,
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
          <button
            className="openk9--search-delete-container-icon"
            title="remove text"
            aria-label={t("remove-text") || "Remove text"}
            style={{
              paddingRight: "16px",
              display: "flex",
              flexDirection: "row",
              padding: "4px 8px",
              gap: "4px",
              alignItems: "center",
              marginRight: "21px",
              background: "inherit",
              border: "none",
            }}
          >
            <div>
              <span
                className="openk9--search-delete-span-icon"
                css={css`
                  cursor: pointer;
                  @media (max-width: 480px) {
                    margin-top: 7px;
                    display: none;
                  }
                `}
                onClick={() => {
                  if (!btnSearch) {
                    selectionsDispatch({
                      type: "set-text",
                      textOnchange: "",
                    });
                  } else {
                    setTextBtn("");
                  }
                }}
              >
                <DeleteLogo />
              </span>
            </div>
          </button>
        </div>
        {btnSearch && (
          <div className="openk9-search-btn-external-container">
            <button
              className="openk9-search-btn-external"
              css={css`
                min-height: 50px;
                min-width: 50px;
                fill: var(
                  --openk9-embeddable-search--primary-background-tab-color
                );
                border: 1px solid
                  var(--openk9-embeddable-search--primary-background-tab-color);
                border-radius: 30px;
                cursor: pointer;
              `}
              onClick={() => {
                selectionsDispatch({
                  type: "set-text",
                  text: textBtn,
                });
                onDetail(null);
                setOpenedDropdown(null);
              }}
            >
              <svg
                aria-hidden="true"
                className="openk9-search-btn-external-icon"
                height="1em"
                viewBox="0 0 512 512"
              >
                <path d="M416 208c0 45.9-14.9 88.3-40 122.7L502.6 457.4c12.5 12.5 12.5 32.8 0 45.3s-32.8 12.5-45.3 0L330.7 376c-34.4 25.2-76.8 40-122.7 40C93.1 416 0 322.9 0 208S93.1 0 208 0S416 93.1 416 208zM208 352a144 144 0 1 0 0-288 144 144 0 1 0 0 288z" />
              </svg>
            </button>
          </div>
        )}
      </div>
    </React.Fragment>
  );
}
