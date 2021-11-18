import React from "react";
import { css } from "styled-components/macro";
import "./index.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSearch } from "@fortawesome/free-solid-svg-icons";
import { PageLayout } from "./components/PageLayout";
import { useDebounce } from "./utils/useDebounce";
import {
  ResultDTO,
  useQueryAnalysis,
  AnalysisResponseEntryDTO,
  AnalysisRequestEntryDTO,
  TokenDTO,
} from "./utils/remote-data";
import { DetailMemo } from "./renderers/Detail";
import { myTheme } from "./utils/myTheme";
import { Results } from "./components/ResultList";
import { TokenSelect } from "./components/TokenSelect";
import { useClickAway } from "./utils/useClickAway";

export function App() {
  const [{ text, selection }, dispatch] = React.useReducer(reducer, {
    text: "",
    selection: [],
  });
  const [detail, setDetail] = React.useState<ResultDTO | null>(null);
  const textDebounced = useDebounce(text, 300);
  const queryAnalysis = useQueryAnalysis({
    searchText: textDebounced,
    tokens: selection,
  });
  const spans = React.useMemo(
    () => calculateSpans(text, queryAnalysis.data?.analysis),
    [queryAnalysis.data?.analysis, text],
  );
  const showSyntax =
    textDebounced === text &&
    queryAnalysis.data !== undefined &&
    !queryAnalysis.isPreviousData;
  const [openedDropdown, setOpenedDropdown] = React.useState<{
    textPosition: number;
    optionPosition: number;
  } | null>(null);
  const clickAwayRef = React.useRef<HTMLDivElement | null>(null);
  useClickAway([clickAwayRef], () => setOpenedDropdown(null));
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
                  return (
                    <TokenSelect
                      key={index}
                      span={span}
                      isOpen={
                        openedDropdown !== null &&
                        openedDropdown.textPosition >= span.start &&
                        openedDropdown.textPosition <= span.end
                      }
                      optionIndex={openedDropdown?.optionPosition ?? null}
                      selected={
                        selection.find(
                          (selection) =>
                            selection.start === span.start &&
                            selection.end === span.end,
                        )?.token ?? null
                      }
                      onSelect={(token) => {
                        dispatch({
                          type: "set-selection",
                          selection: {
                            text: span.text,
                            start: span.start,
                            end: span.end,
                            token,
                          },
                        });
                      }}
                    />
                  );
                })}
            </div>
            <input
              value={text}
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
                  event.currentTarget.selectionDirection === "forward" &&
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
                      span.start <= openedDropdown.textPosition &&
                      span.end > openedDropdown.textPosition,
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
                      selection: {
                        text: span.text,
                        start: span.start,
                        end: span.end,
                        token: option ?? null,
                      },
                    });
                  }
                } else if (event.key === "Escape") {
                  setOpenedDropdown(null);
                }
              }}
            ></input>
          </div>
        </div>
      }
      result={
        <Results
          displayMode={{ type: "virtual" }}
          text={textDebounced}
          onDetail={setDetail}
        />
      }
      detail={detail && <DetailMemo result={detail} />}
    />
  );
}

function calculateSpans(
  text: string,
  analysis: AnalysisResponseEntryDTO[] | undefined,
): AnalysisResponseEntryDTO[] {
  const spans: Array<AnalysisResponseEntryDTO> = [
    { text: "", start: 0, end: 0, tokens: [] },
  ];
  for (let i = 0; i < text.length; ) {
    const found = analysis?.find(({ start }) => i === start);
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
  return spans;
}

type State = { text: string; selection: Array<AnalysisRequestEntryDTO> };
type Action =
  | { type: "set-text"; text: string }
  | {
      type: "set-selection";
      selection: {
        text: string;
        start: number;
        end: number;
        token: TokenDTO | null;
      };
    };
function reducer(state: State, action: Action): State {
  switch (action.type) {
    case "set-text": {
      return {
        text: action.text,
        selection: shiftSelection(state.text, action.text, state.selection),
      };
    }
    case "set-selection": {
      return {
        text: state.text,
        selection: state.selection
          .filter(
            (selection) =>
              !(
                selection.start === action.selection.start &&
                selection.end === action.selection.end
              ),
          )
          .concat(
            action.selection.token
              ? [action.selection as AnalysisRequestEntryDTO]
              : [],
          ),
      };
    }
  }
}

function shiftSelection(
  prevText: string,
  nextText: string,
  prevSelection: Array<AnalysisRequestEntryDTO>,
): Array<AnalysisRequestEntryDTO> {
  if (prevText === nextText) {
    return prevSelection;
  }
  const commonPrefixLength = findCommonPrefixLength(prevText, nextText);
  const commonSuffixLength = findCommonSuffixLength(
    prevText,
    nextText,
    commonPrefixLength,
  );
  const changeStart = commonPrefixLength;
  const changePrevEnd = prevText.length - commonSuffixLength;
  const changeNextEnd = nextText.length - commonSuffixLength;
  const changeDelta = changeNextEnd - changePrevEnd;
  const prefixAttributes = prevSelection.filter(
    (attribute) =>
      attribute.start <= changeStart && attribute.end <= changeStart,
  );
  const deletedAttributes = prevSelection.filter(
    (attribute) =>
      !(attribute.start <= changeStart && attribute.end <= changeStart) &&
      !(attribute.start >= changePrevEnd),
  );
  const suffixAttributes = prevSelection
    .filter((attribute) => attribute.start >= changePrevEnd)
    .map((attribute) => ({
      ...attribute,
      start: attribute.start + changeDelta,
      end: attribute.end + changeDelta,
    }));
  return prefixAttributes.concat(suffixAttributes);
}

function findCommonPrefixLength(a: string, b: string) {
  const length = Math.min(a.length, b.length);
  let prefixLength = 0;
  for (
    ;
    prefixLength < length &&
    a.charCodeAt(prefixLength) === b.charCodeAt(prefixLength);
    ++prefixLength
  ) {}

  return prefixLength;
}

function findCommonSuffixLength(a: string, b: string, startFromIndex: number) {
  const length = Math.min(a.length, b.length) - startFromIndex;
  if (length <= 0) {
    return 0;
  }
  let suffixLength = 0;
  for (
    ;
    suffixLength < length &&
    a.charCodeAt(a.length - suffixLength - 1) ===
      b.charCodeAt(b.length - suffixLength - 1);
    ++suffixLength
  ) {}

  return suffixLength;
}
