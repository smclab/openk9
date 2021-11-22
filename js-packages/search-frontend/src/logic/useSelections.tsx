import React from "react";
import { AnalysisRequestEntryDTO, TokenDTO } from "../utils/remote-data";
import { loadQueryString, saveQueryString } from "../utils/queryString";

export function useSelections() {
  const [state, dispatch] = React.useReducer(
    reducer,
    initial,
    (initial) => loadQueryString<State>() ?? initial,
  );
  React.useEffect(() => {
    saveQueryString(state);
  }, [state]);
  return [state, dispatch] as const;
}

type State = {
  text: string;
  selection: Array<AnalysisRequestEntryDTO & { isAuto?: boolean }>;
};
const initial: State = {
  text: "",
  selection: [],
};
type Action =
  | { type: "set-text"; text: string }
  | {
      type: "set-selection";
      selection: {
        text: string;
        start: number;
        end: number;
        token: TokenDTO | null;
        isAuto?: boolean;
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
  // const deletedAttributes = prevSelection.filter(
  //   (attribute) =>
  //     !(attribute.start <= changeStart && attribute.end <= changeStart) &&
  //     !(attribute.start >= changePrevEnd),
  // );
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
