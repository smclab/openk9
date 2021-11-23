import React from "react";
import { css } from "styled-components/macro";
import "./index.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faLightbulb,
  faSearch,
  faSyncAlt,
} from "@fortawesome/free-solid-svg-icons";
import { PageLayout } from "./components/PageLayout";
import { useDebounce } from "./utils/useDebounce";
import {
  ResultDTO,
  useQueryAnalysis,
  AnalysisResponseEntryDTO,
  AnalysisRequestEntryDTO,
  TokenDTO,
  SearchTokenDTO,
} from "./utils/remote-data";
import { DetailMemo } from "./renderers/Detail";
import { myTheme } from "./utils/myTheme";
import { Results } from "./components/ResultList";
import { TokenSelect } from "./components/TokenSelect";
import { useClickAway } from "./utils/useClickAway";
import { getAutoSelections, useSelections } from "./logic/useSelections";
import { Tooltip } from "./components/Tooltip";

const DEBOUNCE = 300;

export function App() {
  const [autoSelect, setAutoSelect] = React.useState(true);
  const [replaceText, setReplaceText] = React.useState(true);
  const [state, dispatch] = useSelections();
  const [openedDropdown, setOpenedDropdown] = React.useState<{
    textPosition: number;
    optionPosition: number;
  } | null>(null);
  const [detail, setDetail] = React.useState<ResultDTO | null>(null);
  const debounced = useDebounce(state, DEBOUNCE);
  const queryAnalysis = useQueryAnalysis({
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
  const searchQueryMemo = React.useMemo(
    () =>
      deriveSearchQuery(
        spans,
        state.selection.flatMap(({ text, start, end, token }) =>
          token ? [{ text, start, end, token }] : [],
        ),
      ),
    [spans, state.selection],
  );
  const searchQuery = useDebounce(searchQueryMemo, DEBOUNCE);
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
  return (
    <PageLayout
      search={
        <div
          ref={clickAwayRef}
          css={css`
            display: flex;
            border: 1px solid ${myTheme.searchBarBorderColor};
            border-radius: 4px;
            background-color: white;
            align-items: center;
          `}
        >
          <FontAwesomeIcon icon={faSearch} style={{ paddingLeft: "16px" }} />
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
                padding: 16px;
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
                  const onSelect = (token: TokenDTO | null): void => {
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
                  };
                  const isAutoSelected = selection?.isAuto ?? false;
                  return (
                    <TokenSelect
                      key={index}
                      span={span}
                      isOpen={isOpen}
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
                dispatch({ type: "set-text", text: event.currentTarget.value });
                setDetail(null);
              }}
              css={css`
                position: relative;
                flex-grow: 1;
                border: none;
                outline: none;
                padding: 16px;
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
                  }
                } else if (event.key === "Escape") {
                  setOpenedDropdown(null);
                }
              }}
            ></input>
          </div>
          <Tooltip description="Rimpiazza testo quando si seleziona un significato">
            <FontAwesomeIcon
              icon={faSyncAlt}
              style={{
                paddingRight: "16px",
                color: replaceText ? myTheme.redTextColor : "black",
                cursor: "pointer",
              }}
              onClick={() => {
                setReplaceText(!replaceText);
              }}
            />
          </Tooltip>
          <Tooltip description="Seleziona automaticamente il significato più pertinente">
            <FontAwesomeIcon
              icon={faLightbulb}
              style={{
                paddingRight: "16px",
                color: autoSelect ? myTheme.redTextColor : "black",
                cursor: "pointer",
              }}
              onClick={() => {
                setAutoSelect(!autoSelect);
              }}
            />
          </Tooltip>
        </div>
      }
      result={
        <Results
          displayMode={{ type: "virtual" }}
          searchQuery={searchQuery}
          onDetail={setDetail}
        />
      }
      detail={detail && <DetailMemo result={detail} />}
    />
  );
}

function deriveSearchQuery(
  spans: AnalysisResponseEntryDTO[],
  selection: AnalysisRequestEntryDTO[],
) {
  return spans
    .map((span) => ({ ...span, text: span.text.trim() }))
    .filter((span) => span.text)
    .map((span): SearchTokenDTO => {
      const token =
        selection.find(
          (selection) =>
            selection.start === span.start && selection.end === span.end,
        )?.token ?? null;
      if (token) {
        switch (token.tokenType) {
          case "DATASOURCE":
            return { tokenType: "DATASOURCE", values: [token.value] };
          case "DOCTYPE":
            return { tokenType: "DOCTYPE", values: [token.value] };
          case "ENTITY":
            return {
              tokenType: "ENTITY",
              entityType: token.entityType,
              values: [token.value],
            };
          case "TEXT":
            return { tokenType: "TEXT", values: [token.value] };
        }
      }
      return { tokenType: "TEXT", values: [span.text] };
    });
}

function calculateSpans(
  text: string,
  analysis: AnalysisResponseEntryDTO[] | undefined,
): AnalysisResponseEntryDTO[] {
  const spans: Array<AnalysisResponseEntryDTO> = [
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
