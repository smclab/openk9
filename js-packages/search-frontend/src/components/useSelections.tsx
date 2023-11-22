import React from "react";
import {
  AnalysisResponseEntry,
  AnalysisToken,
  SearchToken,
  useOpenK9Client,
} from "./client";
import { loadQueryString, saveQueryString } from "./queryString";
import { containsAtLeastOne } from "../embeddable/Main";

export function useSelections() {
  const [state, dispatch] = React.useReducer(
    reducer,
    initial,
    (initial) => loadQueryString<SelectionsState>() ?? initial,
  );
  
  const [canSave, setCanSave] = React.useState(false);
  const client = useOpenK9Client();
  // React.useEffect(() => {
  //   if (client.authInit)
  //     client.authInit.then(() => {
  //       setCanSave(true);
  //     });
  // }, []);
  // React.useEffect(() => {
  //   if (canSave) {
  //     saveQueryString(state);
  //   }
  // }, [canSave, state]);
  saveQueryString(state);
  return [state, dispatch] as const;
}

export function useSelectionsOnClick() {
  const [state, dispatch] = React.useReducer(
    reducerOnClick,
    initialOnClick,
    (initialOnClick) =>
      loadQueryString<SelectionsStateOnClick>() || initialOnClick,
  );
  
  const [canSave, setCanSave] = React.useState(false);
  const client = useOpenK9Client();
  // React.useEffect(() => {
  //   if (client.authInit)
  //     client.authInit.then(() => {
  //       setCanSave(true);
  //     });
  // }, []);
  // React.useEffect(() => {
  //   if (canSave) {
  //     saveQueryString(state);
  //   }
  // }, [canSave, state]);
  saveQueryString(state);
  return [state, dispatch] as const;
}

export type SelectionsState = {
  text?: string;
  selection: Array<Selection>;
  textOnChange?: string;
  filters: SearchToken[];
};

const initial: SelectionsState = {
  text: "",
  selection: [],
  textOnChange: "",
  filters: [],
};

export type SelectionsAction =
  | { type: "set-text"; text?: string; textOnchange?: string, onClick?:boolean }
  | { type: "reset-search" }
  | {
      type: "set-selection";
      replaceText: boolean;
      selection: Selection;
    }
  | { type: "remove-filter"; filter: SearchToken }
  | { type: "set-filters"; filter: any }
  | { type: "reset-filters" };

 type Selection = {
  text: string;
  textOnChange: string;
  start: number;
  end: number;
  token: AnalysisToken | null;
  isAuto: boolean;
};

export type SelectionsStateOnClick = {
  text: string;
  selection: Array<SelectionOnClick>;
  filters: SearchToken[];
};

const initialOnClick: SelectionsStateOnClick = {
  text: "",
  selection: [],
  filters: [],
};

export type SelectionsActionOnClick =
  | { type: "set-text"; text: string; onClick?:boolean }
  | {
      type: "set-selection";
      replaceText: boolean;
      selection: SelectionOnClick;
    }
  | { type: "remove-filter"; filter: SearchToken }
  | { type: "set-filters"; filter: any }
  | { type: "reset-filters" };

 type SelectionOnClick = {
  text: string;
  start: number;
  end: number;
  token: AnalysisToken | null;
  isAuto: boolean;
};

function reducer(
  state: SelectionsState,
  action: SelectionsAction,
): SelectionsState {
  switch (action.type) {
    case "set-text": {
      return {
        text:  action.onClick?   action.text || state.text || "" :action.text || "",
        filters: state.filters,
        textOnChange: action.textOnchange || state.textOnChange || "",
        selection: shiftSelection(
          state.textOnChange ?? "",
          action.textOnchange || state.textOnChange || "",
          state.selection,
        ),
      };
    }
    case "reset-search": {
      return {
        text: "",
        textOnChange: "",
        filters: state.filters,
        selection: [],
      };
    }
    case "set-selection": {
      const { text, selection } = (() => {
        if (
          action.replaceText ||
          action.selection.token?.tokenType === "AUTOCORRECT"
        ) {
          // const textOnchange = action.selection.textOnChange;
          const tokenText = action.selection.token
            ? getTokenText(action.selection.token)
            : state.textOnChange ||
              "".slice(action.selection.start, action.selection.end);

          const text =
            state.textOnChange ||
            "".slice(0, action.selection.start) +
              tokenText +
              state.textOnChange ||
            "".slice(action.selection.end);
          const selection: Selection | null =
            action.selection.token?.tokenType === "AUTOCORRECT"
              ? null
              : {
                  text: tokenText,
                  textOnChange: tokenText,
                  start: action.selection.start,
                  end: action.selection.start + tokenText.length,
                  token: action.selection.token,
                  isAuto: action.selection.isAuto,
                };
          return {
            text,
            selection,
          };
        } else {
          return { text: state.textOnChange, selection: action.selection };
        }
      })();
      return {
        text,
        textOnChange: state.textOnChange || "",
        filters: state.filters,
        selection: shiftSelection(
          state.textOnChange || "",
          state.textOnChange || "",
          selection
            ? state.selection.filter((s) => !isOverlapping(s, selection))
            : state.selection,
        ).concat(selection ? [selection] : []),
      };
    }
    case "set-filters": {
      return {
        text: state.text,
        filters: [...state.filters, action.filter],
        selection: state.selection,
      };
    }
    case "reset-filters": {
      return {
        text: state.text,
        filters: [],
        selection: state.selection,
      };
    }
    case "remove-filter": {
      const filters = state.filters.filter((token) => {
        const searchToken = action.filter;
        if (searchToken && searchToken.values && token && token.values)
          return !(
            token.suggestionCategoryId === searchToken.suggestionCategoryId &&
            token.isFilter === searchToken.isFilter &&
            token.keywordKey === searchToken.keywordKey &&
            token.tokenType === searchToken.tokenType &&
            containsAtLeastOne(searchToken.values, token.values)
          );
        return true;
      });
      return {
        filters: filters,
        selection: state.selection,
        text: state.text,
      };
    }
  }
}

function reducerOnClick(
  state: SelectionsStateOnClick,
  action: SelectionsActionOnClick,
): SelectionsStateOnClick {
  switch (action.type) {
    case "set-text": {
      return {
        text: action.text,
        filters: state.filters,
        selection: shiftSelectionOnCLick(
          state.text,
          action.text,
          state.selection,
        ),
      };
    }
    case "set-filters": {
      return {
        text: state.text,
        filters: [...state.filters, action.filter],
        selection: state.selection,
      };
    }
    case "reset-filters": {
      return {
        text: state.text,
        filters: [],
        selection: state.selection,
      };
    }
    case "remove-filter": {
      const filters = state.filters.filter((token) => {
        const searchToken = action.filter;
        if (searchToken && searchToken.values && token && token.values)
          return !(
            token.suggestionCategoryId === searchToken.suggestionCategoryId &&
            token.isFilter === searchToken.isFilter &&
            token.keywordKey === searchToken.keywordKey &&
            token.tokenType === searchToken.tokenType &&
            containsAtLeastOne(searchToken.values, token.values)
          );
        return true;
      });
      return {
        filters: filters,
        selection: state.selection,
        text: state.text,
      };
    }
    case "set-selection": {
      const { text, selection } = (() => {
        if (
          action.replaceText ||
          action.selection.token?.tokenType === "AUTOCORRECT"
        ) {
          const tokenText = action.selection.token
            ? getTokenText(action.selection.token)
            : state.text.slice(action.selection.start, action.selection.end);
          const text =
            state.text.slice(0, action.selection.start) +
            tokenText +
            state.text.slice(action.selection.end);
          const selection: SelectionOnClick | null =
            action.selection.token?.tokenType === "AUTOCORRECT"
              ? null
              : {
                  text: tokenText,
                  start: action.selection.start,
                  end: action.selection.start + tokenText.length,
                  token: action.selection.token,
                  isAuto: action.selection.isAuto,
                };
          return {
            text,
            selection,
          };
        } else {
          return { text: state.text, selection: action.selection };
        }
      })();
      return {
        text,
        filters: state.filters,
        selection: shiftSelectionOnCLick(
          state.text,
          text,
          selection
            ? state.selection.filter((s) => !isOverlapping(s, selection))
            : state.selection,
        ).concat(selection ? [selection] : []),
      };
    }
  }
}

function shiftSelection(
  prevText: string,
  nextText: string,
  prevSelection: Array<Selection>,
): Array<Selection> {
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

function shiftSelectionOnCLick(
  prevText: string,
  nextText: string,
  prevSelection: Array<SelectionOnClick>,
): Array<SelectionOnClick> {
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

function getTokenText(token: AnalysisToken) {
  switch (token.tokenType) {
    case "DATASOURCE":
      return token.value;
    case "DOCTYPE":
      return token.value;
    case "ENTITY":
      return token.entityName;
    case "TEXT":
      return token.value;
    case "AUTOCORRECT":
      return token.value;
    case "AUTOCOMPLETE":
      return token.value;
  }
}

export function getAutoSelections(
  selection: Array<{
    text: string;
    start: number;
    end: number;
    token: AnalysisToken | null;
  }>,
  entries: Array<AnalysisResponseEntry>,
) {
  return entries
    .filter(({ text }) => text.length > 0)
    .map(getAutoSelection)
    .filter(isNotNull)
    .filter((entry, index, autoSelections) => {
      return selection
        .concat(autoSelections.slice(0, index))
        .every((selection) => !isOverlapping(entry, selection));
    });
}

function getAutoSelection(entry: AnalysisResponseEntry) {
  const [first, second] = [...entry.tokens].sort((a, b) => a.score - b.score);
  if (first) {
    if (first.tokenType === "AUTOCOMPLETE" || first.tokenType === "AUTOCORRECT")
      return null;
    if (!second || first.score >= second.score * 2) {
      return {
        text: entry.text,
        start: entry.start,
        end: entry.end,
        token: first,
        isAuto: true,
      };
    }
  }
  return null;
}

function isNotNull<Value>(value: Value | null): value is Value {
  return value !== null;
}

export function isOverlapping<
  A extends { start: number; end: number },
  B extends { start: number; end: number },
>(a: A, b: B) {
  return isInRange(a, b.start) || isInRange(a, b.end);
}

function isInRange<R extends { start: number; end: number }>(
  { start, end }: R,
  position: number,
) {
  return position >= start && position <= end;
}
