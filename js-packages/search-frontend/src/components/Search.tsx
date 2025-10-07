import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSearch } from "@fortawesome/free-solid-svg-icons/faSearch";
import { TokenSelect } from "../components/TokenSelect";
import { characterControlType, Configuration } from "../embeddable/entry";
import { AnalysisResponseEntry, AnalysisToken } from "./client";
import { SelectionsAction, SelectionsState } from "./useSelections";
import { DeleteLogo } from "./DeleteLogo";
import { useTranslation } from "react-i18next";
import { useClickAway } from "./useClickAway";

type SearchProps = {
  configuration: Configuration;
  spans: Array<AnalysisResponseEntry>;
  selectionsState: SelectionsState;
  selectionsDispatch(action: SelectionsAction): void;
  showSyntax: boolean;
  btnSearch?: boolean;
  actionOnClick?(): void;
  customMessageSearch?: string;
  htmlKey?: string | undefined | null;
  messageSearchIsVisible?: boolean;
  viewColor?: boolean;
  callbackClickSearch?(): void;
  characterControl?: characterControlType;
  callbackChangeSearch?(text: string): void;
  extraClass?: { input?: string; button?: string } | null | undefined;
};

export function Search({
  configuration,
  spans,
  selectionsState,
  selectionsDispatch,
  showSyntax,
  btnSearch = true,
  htmlKey,
  customMessageSearch,
  messageSearchIsVisible = true,
  viewColor = true,
  actionOnClick,
  callbackClickSearch,
  characterControl,
  extraClass,
  callbackChangeSearch,
}: SearchProps) {
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

  const { setText, search, clearSearch } = setValueSearch({
    isBtn: btnSearch,
    selectionsDispatch,
    characterControl,
    actionSearch: characterControl?.actionCharacter,
  });
  const { t } = useTranslation();

  return (
    <React.Fragment>
      <div
        className="openk9--search-container"
        css={css`
          display: flex;
          align-items: center;
          gap: 10px;
          width: 100%;
          @media (max-width: 480px) {
            flex-direction: row;
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
            border-radius: 8px;
            width: 100%;
            max-height: 50px;
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
                    (s) => s.start === span.start && s.end === span.end,
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
                  const onOptionIndexChange = (i: number) => {
                    setOpenedDropdown((o) =>
                      o ? { ...o, optionPosition: i } : o,
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
                htmlFor="search-openk9"
                className="visually-hidden label-search"
                css={css`
                  border: 0;
                  padding: 0;
                  margin: 0;
                  position: absolute !important;
                  height: 1px;
                  width: 1px;
                  overflow: hidden;
                  clip: rect(1px 1px 1px 1px);
                  clip: rect(1px, 1px, 1px, 1px);
                  clip-path: inset(50%);
                  white-space: nowrap;
                `}
              >
                Search
              </label>
            )}
            <input
              className={`openk9--input-search ${extraClass?.input || ""}`}
              autoComplete="off"
              ref={inputRef}
              id={htmlKey || "search-openk9"}
              aria-describedby="message-search"
              type="text"
              placeholder={t("search") || "search..."}
              value={selectionsState.textOnChange}
              onClick={() => {
                callbackClickSearch && callbackClickSearch();
              }}
              onChange={(event) => {
                setText(event.currentTarget.value);
                callbackChangeSearch &&
                  callbackChangeSearch(event.currentTarget.value);
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
                color: ${viewColor
                  ? showSyntax
                    ? "transparent"
                    : "black"
                  : "black"};
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
                    (s) =>
                      openedDropdown.textPosition > s.start &&
                      openedDropdown.textPosition <= s.end,
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
                  inputRef &&
                    inputRef.current &&
                    search(inputRef?.current?.value || "");
                  if (actionOnClick) actionOnClick();
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
                id="message-search"
                css={css`
                  border: 0;
                  padding: 0;
                  margin: 0;
                  position: absolute !important;
                  height: 1px;
                  width: 1px;
                  overflow: hidden;
                  clip: rect(1px 1px 1px 1px);
                  clip: rect(1px, 1px, 1px, 1px);
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
            className="openk9--search-delete-container-icon"
            title={t("remove-text") || ""}
            aria-label={t("remove-text") || ""}
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
              clearSearch();
            }}
          >
            <div>
              <span
                className="openk9--search-delete-span-icon"
                css={css`
                  cursor: pointer;
                `}
              >
                <DeleteLogo />
              </span>
            </div>
          </button>
        </div>
        {btnSearch && (
          <div className="openk9-search-btn-external-container">
            <button
              className={`openk9-search-btn-external ${
                extraClass?.button || ""
              }`}
              aria-label={t("search-on-website") || "search on website"}
              css={css`
                min-height: 50px;
                min-width: 50px;
                border: 1px solid var(--openk9-embeddable-search--border-color);
                border-radius: 8px;
                cursor: pointer;
                background-color: white;
                display: flex;
                justify-content: center;
                align-items: center;
              `}
              onClick={() => {
                inputRef &&
                  inputRef.current &&
                  search(inputRef.current.value || "");
                if (actionOnClick) actionOnClick();
              }}
            >
              <svg
                aria-hidden="true"
                className="openk9-search-btn-external-icon"
                height="1em"
                viewBox="0 0 512 512"
                fill="gray"
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

function setValueSearch({
  isBtn,
  selectionsDispatch,
  characterControl,
  actionSearch,
}: {
  isBtn: boolean;
  selectionsDispatch: (action: SelectionsAction) => void;
  characterControl?: characterControlType;
  actionSearch?(): void;
}) {
  const setText = (text: string) =>
    !isBtn
      ? selectionsDispatch({
          type: "set-text",
          text,
          textOnchange: text,
        })
      : selectionsDispatch({
          type: "set-text-btn",
          textOnchange: text,
        });

  const search = (text: string) => {
    (!characterControl?.numberOfCharacters ||
      text === "" ||
      text.length > characterControl.numberOfCharacters) &&
      selectionsDispatch({
        type: "set-text",
        text,
        textOnchange: text,
      });
    if (
      characterControl &&
      text.length <= characterControl.numberOfCharacters &&
      text !== ""
    ) {
      actionSearch && actionSearch();
    }
  };

  const clearSearch = () =>
    selectionsDispatch({
      type: "reset-search",
    });

  return { setText, search, clearSearch };
}

export const SearchMemo = React.memo(Search);
