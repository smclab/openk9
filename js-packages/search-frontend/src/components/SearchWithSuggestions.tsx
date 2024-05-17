import { Configuration } from "../embeddable/entry";
import { AnalysisResponseEntry } from "./client";
import { SelectionsAction, SelectionsState } from "./useSelections";
import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSearch } from "@fortawesome/free-solid-svg-icons/faSearch";
import { TokenSelect } from "../components/TokenSelect";
import { AnalysisToken, SearchToken } from "./client";
import { DeleteLogo } from "./DeleteLogo";
import { useTranslation } from "react-i18next";
import { useClickAway } from "./useClickAway";
import { SearchSvg } from "../svgElement/SearchSvg";

type SearchWithSuggestions = {
  configuration: Configuration;
  spans: Array<AnalysisResponseEntry>;
  selectionsState: SelectionsState;
  selectionsDispatch(action: SelectionsAction): void;
  showSyntax: boolean;
  btnSearch?: boolean;
  actionOnClick?(): void;
  callbackSearch?(): void;
  customMessageSearch?: string;
  htmlKey?: string | undefined | null;
  messageSearchIsVisible?: boolean;
  viewColor?: boolean;
  placeholder?: string;
  labelIcon?: string;
};
export function SearchWithSuggestions({
  configuration,
  spans,
  selectionsState,
  selectionsDispatch,
  showSyntax,
  htmlKey,
  customMessageSearch,
  messageSearchIsVisible = true,
  viewColor = true,
  callbackSearch,
  actionOnClick,
  placeholder,
  labelIcon,
}: SearchWithSuggestions) {
  const autoSelect = configuration.searchAutoselect;
  const replaceText = configuration.searchReplaceText;
  const [openedDropdown, setOpenedDropdown] = React.useState<{
    textPosition: number;
    optionPosition: number;
  } | null>({ textPosition: 0, optionPosition: 1 });
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
  const { setText } = setValueSearch({
    selectionsDispatch: selectionsDispatch,
  });

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
                @media (max-width: 480px) {
                  display: none;
                }
              `}
            >
              {showSyntax &&
                spans.map((span, index) => {
                  const isOpen =
                    openedDropdown !== null &&
                    openedDropdown.textPosition > span.start &&
                    openedDropdown.textPosition <= span.end &&
                    span.tokens.length > 0;
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
                        textOnChange: span.text,
                        start: span.start,
                        end: span.end,
                        token,
                        isAuto: autoSelect,
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
                      setOpenedDropdown={setOpenedDropdown}
                      isColorSearch={viewColor}
                    />
                  );
                })}
            </div>
            {!htmlKey && (
              <label
                htmlFor="openk9--input-search"
                className="visually-hidden label-search"
                css={css`
                  border: 0;
                  padding: 0;
                  margin: 0;
                  position: absolute !important;
                  height: 1px;
                  width: 1px;
                  overflow: hidden;
                  clip: rect(
                    1px 1px 1px 1px
                  ); /* IE6, IE7 - a 0 height clip, off to the bottom right of the visible 1px box */
                  clip: rect(
                    1px,
                    1px,
                    1px,
                    1px
                  ); /*maybe deprecated but we need to support legacy browsers */
                  clip-path: inset(50%);
                  white-space: nowrap;
                `}
              >
                Search
              </label>
            )}
            <input
              className="openk9--input-search"
              autoComplete="off"
              ref={inputRef}
              id={"search-openk9-header"}
              aria-describedby="message-search"
              type="text"
              placeholder={placeholder || t("search") || "search..."}
              value={selectionsState.textOnChange}
              onChange={(event) => {
                setText(event.currentTarget.value);
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
                color: black;
                @media (max-width: 480px) {
                  color: black;
                }
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
                          : 1,
                    });
                  }
                } else if (event.key === "ArrowUp") {
                  event.preventDefault();
                  if (openedDropdown && openedDropdown.optionPosition > 0) {
                    setOpenedDropdown({
                      textPosition: openedDropdown.textPosition,
                      optionPosition:
                        openedDropdown.optionPosition === 1
                          ? span?.tokens.length || 1
                          : openedDropdown.optionPosition - 1,
                    });
                  }
                } else if (event.key === "Enter") {
                  event.preventDefault();
                  if (actionOnClick) {
                    actionOnClick();
                  }
                  if (span && option) {
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
                    if (replaceText && option) {
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
                  }
                }
              }}
            ></input>
            {messageSearchIsVisible && (
              <p
                className="visually-hidden"
                id="message-search-header"
                css={css`
                  border: 0;
                  padding: 0;
                  margin: 0;
                  position: absolute !important;
                  height: 1px;
                  width: 1px;
                  overflow: hidden;
                  clip: rect(
                    1px 1px 1px 1px
                  ); /* IE6, IE7 - a 0 height clip, off to the bottom right of the visible 1px box */
                  clip: rect(
                    1px,
                    1px,
                    1px,
                    1px
                  ); /*maybe deprecated but we need to support legacy browsers */
                  clip-path: inset(50%);
                  white-space: nowrap;
                `}
              >
                {customMessageSearch ||
                  t(
                    "insert-text-to-set-the-value-or-use-up-and-down-arrow-keys-to-navigate-the-suggestion-box",
                  ) ||
                  "insert text to set the value or use up and down arrow keys to navigate the suggestion box"}
              </p>
            )}
          </div>
          <button
            id="search-header-icon"
            className="openk9--search-search-container-icon"
            title={"avvia ricerca"}
            aria-label={labelIcon}
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
            onClick={() => {
              if (callbackSearch) callbackSearch();
            }}
          >
            <div>
              <span
                className="openk9--search-search-span-icon"
                css={css`
                  cursor: pointer;
                  @media (max-width: 480px) {
                    margin-top: 7px;
                    display: none;
                  }
                `}
              >
                <SearchSvg />
              </span>
            </div>
          </button>
        </div>
      </div>
    </React.Fragment>
  );
}

function setValueSearch({
  selectionsDispatch,
}: {
  selectionsDispatch: (action: SelectionsAction) => void;
}) {
  const setText = (text: string) =>
    selectionsDispatch({
      type: "set-text",
      textOnchange: text,
      text,
    });

  return { setText };
}
