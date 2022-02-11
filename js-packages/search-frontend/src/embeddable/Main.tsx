import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faLightbulb,
  faSearch,
  faSyncAlt,
} from "@fortawesome/free-solid-svg-icons";
import { useDebounce } from "../components/useDebounce";
import { useQueryAnalysis } from "../components/remote-data";
import { useTabTokens } from "../components/useTabTokens";
import { DetailMemo } from "../components/Detail";
import { myTheme } from "../components/myTheme";
import { Results } from "../components/ResultList";
import { TokenSelect } from "../components/TokenSelect";
import { useClickAway } from "../components/useClickAway";
import { getAutoSelections, useSelections } from "../components/useSelections";
import { Tooltip } from "../components/Tooltip";
import { useLoginInfo } from "../components/useLogin";
import { LoginInfoComponent } from "../components/LoginInfo";
import { OpenK9ConfigFacade } from "./entry";
import ReactDOM from "react-dom";
import {
  AnalysisRequestEntry,
  AnalysisResponseEntry,
  AnalysisToken,
  GenericResultItem,
  LoginInfo,
  getSuggestionCategories,
  SearchToken,
} from "@openk9/rest-api";
import { Logo } from "../components/Logo";
import { useQuery } from "react-query";
import { isEqual } from "lodash";
import { SimpleErrorBoundary } from "../components/SimpleErrorBoundary";
import { FilterCategory } from "../components/FilterCategory";
import { useRenderers } from "../components/useRenderers";

const DEBOUNCE = 300;

type MainProps = { config: OpenK9ConfigFacade };
export function Main({ config }: MainProps) {
  const login = useLoginInfo();
  const [autoSelect, setAutoSelect] = React.useState(true);
  const [replaceText, setReplaceText] = React.useState(true);
  const [state, dispatch] = useSelections();
  const [openedDropdown, setOpenedDropdown] = React.useState<{
    textPosition: number;
    optionPosition: number;
  } | null>(null);
  const [detail, setDetail] = React.useState<GenericResultItem<unknown> | null>(
    null,
  );
  const tabs = useTabTokens(login.state.loginInfo ?? null);
  const [selectedTabIndex, setSelectedTabIndex] = React.useState(0);
  const tabTokens = React.useMemo(
    () => tabs[selectedTabIndex]?.tokens ?? [],
    [selectedTabIndex, tabs],
  );
  const debounced = useDebounce(state, DEBOUNCE);
  const queryAnalysis = useQueryAnalysis(login.state?.loginInfo ?? null, {
    searchText: debounced.text,
    tokens: debounced.selection.flatMap(({ text, start, end, token }) =>
      token ? [{ text, start, end, token }] : [],
    ),
  });
  const spans = React.useMemo(
    () => calculateSpans(state.text, queryAnalysis.data?.analysis),
    [queryAnalysis.data?.analysis, state.text],
  );
  const showSyntax =
    state.text === debounced.text &&
    queryAnalysis.data !== undefined &&
    !queryAnalysis.isPreviousData;
  const clickAwayRef = React.useRef<HTMLDivElement | null>(null);
  useClickAway([clickAwayRef], () => setOpenedDropdown(null));
  const {
    onQueryStateChange,
    queryState: { hiddenSearchQuery },
  } = config;
  const [filterSearchTokens, setFilterSearchTokens] = React.useState<
    Array<SearchToken>
  >([]);
  const addFilterSearchToken = React.useCallback((searchToken: SearchToken) => {
    setFilterSearchTokens((tokens) => [...tokens, searchToken]);
  }, []);
  const removeFilterSearchToken = React.useCallback(
    (searchToken: SearchToken) => {
      setFilterSearchTokens((tokens) =>
        tokens.filter((token) => !isEqual(token, searchToken)),
      );
    },
    [],
  );
  const searchQueryMemo = React.useMemo(
    () => [
      ...tabTokens,
      ...hiddenSearchQuery,
      ...filterSearchTokens,
      ...deriveSearchQuery(
        spans,
        state.selection.flatMap(({ text, start, end, token }) =>
          token ? [{ text, start, end, token }] : [],
        ),
      ),
    ],
    [spans, state.selection, tabTokens, hiddenSearchQuery, filterSearchTokens],
  );
  const searchQuery = useDebounce(searchQueryMemo, DEBOUNCE);
  React.useEffect(() => {
    onQueryStateChange?.({
      hiddenSearchQuery: searchQuery,
    });
  }, [onQueryStateChange, searchQuery]);
  React.useEffect(() => {
    if (
      autoSelect &&
      queryAnalysis.data &&
      queryAnalysis.data.searchText === state.text
    ) {
      const autoSelections = getAutoSelections(
        state.selection,
        queryAnalysis.data.analysis,
      );
      for (const selection of autoSelections) {
        dispatch({
          type: "set-selection",
          replaceText: false,
          selection,
        });
      }
    }
  }, [
    autoSelect,
    dispatch,
    queryAnalysis.data,
    replaceText,
    state.selection,
    state.text,
  ]);
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
  React.useEffect(() => {
    setDetail(null);
  }, [searchQuery]);
  const suggestionCategories = useSuggestionCategories(
    login.state.loginInfo ?? null,
  );
  const renderers = useRenderers(login.state.loginInfo ?? null);
  return (
    <React.Fragment>
      {config.search !== null &&
        ReactDOM.createPortal(
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
              style={{ paddingLeft: "16px", color: myTheme.grayTexColor }}
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
                    const selection = state.selection.find(
                      (selection) =>
                        selection.start === span.start &&
                        selection.end === span.end,
                    );
                    const selected = selection?.token ?? null;
                    const onSelect = (token: AnalysisToken | null): void => {
                      dispatch({
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
                value={state.text}
                onChange={(event) => {
                  dispatch({
                    type: "set-text",
                    text: event.currentTarget.value,
                  });
                  setDetail(null);
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
                      dispatch({
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
                    : myTheme.grayTexColor,
                  cursor: "pointer",
                }}
                onClick={() => {
                  setReplaceText(!replaceText);
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
                    : myTheme.grayTexColor,
                  cursor: "pointer",
                }}
                onClick={() => {
                  setAutoSelect(!autoSelect);
                }}
              />
            </Tooltip>
          </div>,
          config.search,
        )}
      {config.tabs !== null &&
        ReactDOM.createPortal(
          <div
            css={css`
              display: flex;
              padding: 0px 16px;
            `}
          >
            {tabs.map((tab, index) => {
              const isSelected = index === selectedTabIndex;
              return (
                <div
                  key={index}
                  css={css`
                    padding: 8px 16px;
                    color: ${isSelected
                      ? "var(--openk9-embeddable-search--primary-color)"
                      : ""};
                    border-bottom: 2px solid
                      ${isSelected
                        ? "var(--openk9-embeddable-search--active-color)"
                        : "transparent"};
                    cursor: pointer;
                    font-size: 0.8rem;
                    color: ${myTheme.grayTexColor};
                    user-select: none;
                  `}
                  onClick={() => {
                    setSelectedTabIndex(index);
                    setFilterSearchTokens([]);
                    window.OpenK9.queryState = { hiddenSearchQuery: [] }; // DEBT: do not use global api
                  }}
                >
                  {tab.label.toUpperCase()}
                </div>
              );
            })}
          </div>,
          config.tabs,
        )}
      {config.filters !== null &&
        ReactDOM.createPortal(
          <div>
            {suggestionCategories.data?.slice(2).map((suggestionCategory) => {
              return (
                <FilterCategory
                  key={suggestionCategory.suggestionCategoryId}
                  suggestionCategoryName={suggestionCategory.name}
                  suggestionCategoryId={suggestionCategory.suggestionCategoryId}
                  tokens={searchQuery}
                  onAdd={addFilterSearchToken}
                  onRemove={removeFilterSearchToken}
                />
              );
            })}
          </div>,
          config.filters,
        )}
      {config.results !== null &&
        ReactDOM.createPortal(
          <Results
            renderers={renderers}
            loginInfo={login.state?.loginInfo ?? null}
            displayMode={{ type: "virtual" }}
            searchQuery={searchQuery}
            onDetail={setDetail}
          />,
          config.results,
        )}
      {config.details !== null &&
        ReactDOM.createPortal(
          detail ? (
            <SimpleErrorBoundary>
              <DetailMemo renderers={renderers} result={detail} />
            </SimpleErrorBoundary>
          ) : (
            <NoDetail />
          ),
          config.details,
        )}
      {config.login !== null &&
        ReactDOM.createPortal(
          <LoginInfoComponent
            loginState={login.state}
            onLogin={login.login}
            onLogout={login.logout}
          />,
          config.login,
        )}
    </React.Fragment>
  );
}

function deriveSearchQuery(
  spans: AnalysisResponseEntry[],
  selection: AnalysisRequestEntry[],
) {
  return spans
    .map((span) => ({ ...span, text: span.text.trim() }))
    .filter((span) => span.text)
    .map((span): SearchToken => {
      const token =
        selection.find(
          (selection) =>
            selection.start === span.start && selection.end === span.end,
        )?.token ?? null;
      if (token) {
        switch (token.tokenType) {
          case "DATASOURCE":
            return {
              tokenType: "DATASOURCE",
              values: [token.value],
              filter: false,
            };
          case "DOCTYPE":
            return {
              tokenType: "DOCTYPE",
              keywordKey: "type",
              values: [token.value],
              filter: false,
            };
          case "ENTITY":
            return {
              tokenType: "ENTITY",
              keywordKey: token.keywordKey,
              entityType: token.entityType,
              values: [token.value],
              filter: false,
            };
          case "TEXT":
            return {
              tokenType: "TEXT",
              keywordKey: token.keywordKey,
              values: [token.value],
              filter: false,
            };
        }
      }
      return { tokenType: "TEXT", values: [span.text], filter: false };
    });
}

function calculateSpans(
  text: string,
  analysis: AnalysisResponseEntry[] | undefined,
): AnalysisResponseEntry[] {
  const spans: Array<AnalysisResponseEntry> = [
    { text: "", start: 0, end: 0, tokens: [] },
  ];
  for (let i = 0; i < text.length; ) {
    const found = analysis?.find(
      ({ start, text }) => i === start && text !== "",
    );
    if (found) {
      spans.push(found);
      i += found.text.length;
      spans.push({ text: "", start: i, end: i, tokens: [] });
    } else {
      const last = spans[spans.length - 1];
      last.text += text[i];
      last.end += 1;
      i += 1;
    }
  }
  return spans.filter((span) => span.text);
}

function NoDetail() {
  return (
    <div
      css={css`
        color: ${myTheme.grayTexColor};
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
      `}
    >
      <Logo size={128} />
      <h3>No details</h3>
      <div>Move the mouse over a result to see details about it</div>
    </div>
  );
}

function useSuggestionCategories(loginInfo: LoginInfo | null) {
  return useQuery(
    ["suggestion-categories", loginInfo],
    async ({ queryKey }) => {
      const result = await getSuggestionCategories(loginInfo);
      return result;
    },
  );
}
