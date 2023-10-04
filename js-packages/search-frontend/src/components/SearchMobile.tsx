import { useTranslation } from "react-i18next";
import { SearchDateRange } from "../embeddable/Main";
import {
  Configuration,
  ConfigurationUpdateFunction,
} from "../embeddable/entry";
import { ModalDetail } from "./ModalDetail";
import {
  AnalysisResponseEntry,
  AnalysisToken,
  GenericResultItem,
  SearchToken,
  SortField,
} from "./client";
import { useClickAway } from "./useClickAway";
import {
  SelectionsAction,
  SelectionsActionOnClick,
  SelectionsState,
  SelectionsStateOnClick,
} from "./useSelections";
import React from "react";
import { css } from "styled-components/macro";
import { ArrowLeftSvg } from "../svgElement/ArrowLeftSvg";
import { SearchSvg } from "../svgElement/SearchSvg";
import { DeleteLogo } from "./DeleteLogo";

type SearchMobileProps = {
  configuration: Configuration;
  onConfigurationChange: ConfigurationUpdateFunction;
  onDetail(detail: GenericResultItem<unknown> | null): void;
  spans: Array<AnalysisResponseEntry>;
  selectionsState: SelectionsState | SelectionsStateOnClick;
  selectionsDispatch(action: SelectionsAction | SelectionsActionOnClick): void;
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
  isVisible: boolean;
  setIsVisible: React.Dispatch<React.SetStateAction<boolean>> | undefined;
};

export function SearchMobile({
  configuration,
  spans,
  selectionsState,
  selectionsDispatch,
  onDetail,
  showSyntax,
  isMobile,
  isVisible,
  setIsVisible,
}: SearchMobileProps) {
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
  const { t } = useTranslation();

  //utili durante l'implementazione del cambio dei colori del query analisys
  // console.log(spans, onselect);
  // selectionsState.selection.forEach((selection) => {
  //   console.log(selection.token);
  // });

  React.useEffect(() => {
    if (isVisible && inputRef.current) {
      inputRef.current?.focus();
    }
  }, [isVisible]);

  const componet = (
    <React.Fragment>
      <div
        css={css`
          @media (max-width: 480px) {
            padding-inline: 15px;
            display: flex;
            flex-direction: column;
            gap: 20px;
            height: 100%;
            background-color: #d4d4d8;
          }
        `}
      >
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
            <button
              css={css`
                border: none;
                background: inherit;
                margin-left: 7px;
                margin-top: 7px;
              `}
              onClick={() => {
                if (setIsVisible) setIsVisible(false);
              }}
            >
              <ArrowLeftSvg size="20" />
            </button>

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
              ></div>
              <label htmlFor="search-openk9-modal" className="visually-hidden">
                Search
              </label>
              <input
                className="openk9--input-search"
                autoComplete="off"
                ref={inputRef}
                enterKeyHint="search"
                onKeyUp={(event) => {
                  if (setIsVisible && event.key === "Enter")
                    setIsVisible(false);
                }}
                id="search-openk9-modal"
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
                  width: 100%;
                  color: ${autoSelect ? "black" : "blue"};
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
                      textPosition: event.currentTarget
                        .selectionStart as number,
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
            <div
              css={css`
                display: flex;
              `}
            >
              <button
                className="openk9--search-delete-container-icon"
                title="remove text"
                aria-label="remove-text"
                style={{
                  alignItems: "center",
                  background: "inherit",
                  border: "none",
                }}
                onClick={() => {
                  selectionsDispatch({
                    type: "set-text",
                    text: "",
                  });
                }}
              >
                <div>
                  <span
                    className="openk9--search-delete-span-icon"
                    css={css`
                      cursor: pointer;
                      @media (max-width: 480px) {
                        margin-top: 7px;
                      }
                    `}
                  >
                    <DeleteLogo />
                  </span>
                </div>
              </button>
              <button
                className="openk9--search-delete-container-icon"
                title="search"
                aria-label="search"
                style={{
                  paddingRight: "16px",
                  display: "flex",
                  flexDirection: "row",
                  padding: "4px 8px",
                  gap: "4px",
                  alignItems: "center",
                  marginRight: "8px",
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
                      }
                    `}
                  >
                    <SearchSvg />
                  </span>
                </div>
              </button>
            </div>
          </div>
        </div>
        <div>
          {spans.map((span, index) => {
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
              <TokenSelectMobile
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
                setIsVisible={setIsVisible}
              />
            );
          })}
        </div>
      </div>
    </React.Fragment>
  );
  if (!isVisible) return null;
  document.body.style.overflow = "hidden";
  return <ModalDetail padding="0px" background="white" content={componet} />;
}
export const SearchMobileMemo = React.memo(SearchMobile);

type TokenSelectMobileProps = {
  span: AnalysisResponseEntry;
  onSelect(token: AnalysisToken | null): void;
  onSelectText(token: AnalysisToken | null): void;
  selected: AnalysisToken | null;
  isOpen: boolean;
  selectionsDispatch: (action: SelectionsAction) => void;
  optionIndex: number | null;
  onOptionIndexChange(optionIndex: number): void;
  isAutoSlected: boolean;
  setIsVisible: React.Dispatch<React.SetStateAction<boolean>> | undefined;
  setOpenedDropdown: React.Dispatch<
    React.SetStateAction<{
      textPosition: number;
      optionPosition: number;
    } | null>
  >;
};
export function TokenSelectMobile({
  span,
  onSelect,
  selected,
  isOpen,
  optionIndex,
  onOptionIndexChange,
  isAutoSlected,
  setOpenedDropdown,
  onSelectText,
  selectionsDispatch,
  setIsVisible,
}: TokenSelectMobileProps) {
  const isInteractive = span.tokens.length > 0;
  const [subtitle, setSubtitle] = React.useState(false);
  const { t } = useTranslation();

  const entryStyle = (isSelected: boolean, isHighlighted: boolean) => css`
    padding: 8px 16px;
    border-bottom: 1px solid #b09c9c12;
    :hover {
    }
    cursor: pointer;
    border-left: ${isSelected
      ? `8px solid var(--openk9-embeddable-search--active-color)`
      : "none"};
    padding-left: ${isSelected ? "8px" : "16px"};
  `;
  const deseleziona = (isSelected: boolean) => css`
    padding: 8px 16px;
    :hover {
    }
    background-color: ${"var(--openk9-embeddable-search--secondary-background-color)"};
    cursor: ${!isSelected ? "" : "not-allowed"};
  `;
  return (
    <div className="openk9-token-select-container" css={css``}>
      {isOpen && isInteractive && (
        <div
          className="openk9-token-select-is-open"
          css={css`
            left: 0px;
            background-color: var(
              --openk9-embeddable-search--primary-background-color
            );
            border: 1px solid var(--openk9-embeddable-search--border-color);
            border-radius: 4px;
            z-index: 2;
          `}
        >
          <div
            className="openk9-enter-token"
            onMouseEnter={() => {
              onOptionIndexChange(0);
            }}
            css={css`
              ${deseleziona(selected === null)};
              padding: 0;
            `}
          >
            {selected && (
              <button
                className="openk9-token-select-uncheck"
                css={css`
                  display: flex;
                  justify-content: space-between;
                  align-items: baseline;
                  color: var(--openk9-embeddable-search--secondary-text-color);
                  text-decoration: underline;
                  cursor: ${selected && subtitle ? "pointer" : "auto"};
                  width: 100%;
                  height: 100%;
                  border: none;
                  padding: 8px 16px;
                `}
              >
                <div
                  onClick={() => {
                    if (selected) onSelect(null);
                  }}
                >
                  {t("uncheck")}
                </div>
              </button>
            )}
          </div>
          {span.tokens.map((option, index) => {
            const isSelected =
              option.tokenType === selected?.tokenType &&
              option.value === selected.value;
            const isHighlighted = optionIndex === index + 1;
            return (
              <div
                key={index}
                onClick={() => {
                  if (option.tokenType === "AUTOCOMPLETE") {
                    if (setIsVisible) setIsVisible(false);
                    onSelectText(option);
                  } else {
                    if (setIsVisible) setIsVisible(false);
                    onSelect(option);
                  }
                }}
                className="openk9-token-select-container-highlighted"
                css={css`
                  ${entryStyle(isSelected, isHighlighted)};
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  border-top: ${index !== 0 ? "0.5px solid #E4E4E7;" : "none"};
                `}
              >
                {"keywordName" in option && (
                  <React.Fragment>
                    <div
                      css={css`
                        padding-inline: 5px;
                        border: ${index !== 0
                          ? "0.5px solid #E4E4E7;"
                          : "none"};
                      `}
                    ></div>
                    <strong
                      className="openk9-token-select-highlighted"
                      css={css`
                        margin-right: 8px;
                      `}
                    >
                      {option.keywordName}:
                    </strong>
                  </React.Fragment>
                )}
                <FactoryTokenType option={option} />
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
type Status =
  | "can-select"
  | "has-selected"
  | "auto-selected"
  | "not-interactive";
const statusStyles: Record<Status, any> = {
  "can-select": css`
    color: var(--openk9-embeddable-search--primary-color);
  `,
  "auto-selected": css`
    color: lightseagreen;
  `,
  "has-selected": css`
    color: dodgerblue;
  `,
  "not-interactive": css`
    color: black;
  `,
};
function getTokenLabel(token: AnalysisToken) {
  switch (token.tokenType) {
    case "DATASOURCE":
      return token.value;
    case "DOCTYPE":
      return token.value;
    case "ENTITY":
      return token.entityName;
    case "TEXT":
      return token.value;
  }
}

function FactoryTokenType({
  option,
}: {
  option: AnalysisToken & {
    score: number;
  };
}) {
  switch (option.tokenType) {
    case "AUTOCOMPLETE":
      return (
        <React.Fragment>
          <div
            className="openk9-token-select-factory-autocomplete"
            css={css`
              display: flex;
              font-family: "Helvetica";
              font-style: normal;
              font-weight: 400;
              font-size: 15px;
              line-height: 17px;
            `}
          >
            {option.value}
          </div>
        </React.Fragment>
      );
      break;
    case "AUTOCORRECT":
      return (
        <span className="openk9-token-select-factory-autocorrect">
          Did you mean? <strong>{option.value}</strong>
        </span>
      );
    default:
      return (
        <React.Fragment>
          <div
            className="openk9-token-select-factory-create"
            css={css`
              display: flex;
              font-family: "Helvetica";
              font-style: normal;
              font-weight: 400;
              font-size: 15px;
              line-height: 17px;
              max-width: 60%;
            `}
          >
            {getTokenLabel(option)}
          </div>
          <div
            className="openk9-token-select-factory-container-option-label"
            style={{
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              padding: "4px 8px",
              gap: "4px",
              height: "15px",
              background: "#FFFFFF",
              border:
                "1px solid var(--openk9-embeddable-search--secondary-active-color)",
              borderRadius: "20px",
              marginLeft: "10px",
            }}
          >
            <div
              className="openk9-token-select-factory-option-label"
              css={css`
                color: var(--openk9-embeddable-search--secondary-active-color);
                margin-bottom: 13px;
                font-size: 12px;
                display: block;
                margin-block-start: 1em;
                margin-block-end: 1em;
                margin-inline-start: 0px;
                margin-inline-end: 0px;
              `}
            >
              {option.label}
            </div>
          </div>
        </React.Fragment>
      );
      break;
  }
}
