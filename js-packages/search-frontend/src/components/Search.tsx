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
import { FilterHorizontalSvg } from "../svgElement/FilterHorizontalSvg";
import { SortResultList } from "../renderer-components";
import { ModalDetail } from "./ModalDetail";
import { FilterSvg } from "../svgElement/FiltersSvg";
import { FiltersHorizontalMemo } from "./FiltersHorizontal";

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
  isMobile: boolean;
  setSortResult: (sortResultNew: SortField) => void;
  searchQuery: SearchToken[];
  onAddFilterToken: (searchToken: SearchToken) => void;
  onRemoveFilterToken: (searchToken: SearchToken) => void;
  filtersSelect: SearchToken[];
  sort: SortField[];
  dynamicFilters: boolean;
};
export function Search({
  configuration,
  spans,
  selectionsState,
  selectionsDispatch,
  onDetail,
  showSyntax,
  setSortResult,
  searchQuery,
  onAddFilterToken,
  onRemoveFilterToken,
  onConfigurationChange,
  sort,
  dynamicFilters,
  filtersSelect,
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
  const [isVisibleFilters, setIsVisibleFilters] = React.useState(false);
  const { t } = useTranslation();
  return (
    <div
      className="openk9--search-container"
      css={css`
        margin-top: 12px;
        display: flex;
        align-items: center;
        gap: 10px;
        margin-inline: 16px;
        @media (max-width: 480px) {
          flex-direction: column;
        }
      `}
    >
      <style type="text/css">
        {`
        .openk9-focusable:has(input:focus){
          border:1px solid  var(--openk9-embeddable-search--active-color);
        }
    `}
      </style>
      <div
        ref={clickAwayRef}
        className="openk9-embeddable-search--input-container openk9-focusable"
        css={css`
          display: flex;
          align-items: center;
          border-radius: 40px;
          width: 100%;
          max-height: 45px;
          @media (max-width: 480px) {
            width: 100%;
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
              color: ${showSyntax ? "transparent" : "inherit"};
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
          </div>
        </button>
      </div>
      <div
        css={css`
          @media (max-width: 480px) {
            display: flex;
            gap: 10px;
            align-items: center;
            width: 100%;
            justify-content: space-between;
          }
        `}
      >
        <div
          css={css`
            @media (max-width: 480px) {
              width: 100%;
            }
          `}
        >
          {/* <DataRangePicker
            onChange={onDateRangeChange}
            calendarDate={dateRange}
          /> */}
          <SortResultList
            setSortResult={setSortResult}
            background="white"
            minHeight="40px"
            color="#7e7e7e"
          />
        </div>
        <div
          css={css`
            @media (min-width: 480px) {
              display: none;
            }
          `}
        >
          <button
            css={css`
              padding: 6px 10px;
              border: 1px solid var(--openk9-embeddable-search--border-color);
              background: white;
              border-radius: 50px;
            `}
            onClick={() => {
              setIsVisibleFilters(true);
            }}
          >
            <FilterHorizontalSvg />
          </button>
        </div>
      </div>
      {isVisibleFilters && (
        <ModalDetail
          padding="0px"
          background="white"
          content={
            <React.Fragment>
              <div
                css={css`
                  display: flex;
                  justify-content: space-beetween;
                  background: white;
                `}
              >
                <div
                  className="openk9-filter-list-container-title box-title"
                  css={css`
                    padding: 0px 16px;
                    width: 100%;
                    background: #fafafa;
                    padding-top: 20px;
                    padding-bottom: 13px;
                    display: flex;
                  `}
                >
                  <div
                    className="openk9-filter-list-container-internal-title "
                    css={css`
                      display: flex;
                      gap: 5px;
                    `}
                  >
                    <span>
                      <FilterSvg />
                    </span>
                    <span className="openk9-filters-list-title title">
                      <h2
                        css={css`
                          font-style: normal;
                          font-weight: 700;
                          font-size: 18px;
                          height: 18px;
                          line-height: 22px;
                          display: flex;
                          align-items: center;
                          color: #3f3f46;
                          margin: 0;
                        `}
                      >
                        {t("filters")}
                      </h2>
                    </span>
                  </div>
                </div>
                <button
                  onClick={() => {
                    setIsVisibleFilters(false);
                  }}
                  style={{ backgroundColor: "#FAFAFA", border: "none" }}
                >
                  <DeleteLogo heightParam={15} widthParam={15} />
                </button>
              </div>
              <FiltersHorizontalMemo
                searchQuery={searchQuery}
                onAddFilterToken={onAddFilterToken}
                onRemoveFilterToken={onRemoveFilterToken}
                onConfigurationChange={onConfigurationChange}
                onConfigurationChangeExt={() => setIsVisibleFilters(false)}
                filtersSelect={configuration.filterTokens}
                sort={sort}
                dynamicFilters={dynamicFilters}
              />
            </React.Fragment>
          }
        />
      )}
    </div>
  );
}
