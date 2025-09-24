import React from "react";
import {
  AnalysisResponseEntry,
  AnalysisToken,
  SearchToken,
  useOpenK9Client,
} from "./client";
import { loadQueryString, saveQueryString } from "./queryString";
import { containsAtLeastOne } from "../embeddable/Main";
import { queryStringValues } from "../embeddable/entry";

type Range = [number, number];

export function useSelections({
  useKeycloak = true,
  useQueryString = true,
  defaultString = "",
  queryStringValues,
}: {
  useKeycloak?: boolean;
  useQueryString?: boolean;
  defaultString?: string;
  queryStringValues: queryStringValues;
}) {
  const defaultSearch: SelectionsState = {
    text: defaultString,
    selection: [],
    textOnChange: defaultString,
    filters: [],
    range: [0, 10],
    commitId: 0,
  };

  const [state, dispatch] = React.useReducer(reducer, defaultSearch, (s) =>
    loadQueryString<SelectionsState>(s),
  );

  const [canSave, setCanSave] = React.useState(false);
  const client = useOpenK9Client();

  React.useEffect(() => {
    if (useKeycloak && client.authInit) {
      client.authInit.then(() => setCanSave(true));
    } else if (!useKeycloak) {
      setCanSave(true);
    }
  }, []);

  React.useEffect(() => {
    if (useQueryString && (canSave || !useKeycloak)) {
      saveQueryString(state, queryStringValues);
    }
  }, [canSave, state]);

  return [state, dispatch] as const;
}

export type SelectionsState = {
  text: string;
  selection: Array<Selection>;
  textOnChange: string;
  filters: SearchToken[];
  range: Range;
  commitId: number;
};

export type SelectionsAction =
  | { type: "set-text"; text: string; textOnchange: string; pageSize?: number }
  | { type: "set-text-btn"; textOnchange: string }
  | { type: "reset-search" }
  | {
      type: "set-selection";
      replaceText: boolean;
      selection: Selection;
      textEntity?: string;
    }
  | { type: "remove-filter"; filter: SearchToken }
  | { type: "set-filters"; filter: SearchToken }
  | { type: "reset-filters" }
  | { type: "set-range"; range: Range }
  | { type: "set-page-size"; pageSize: number };

export type Selection = {
  text: string;
  textOnChange: string;
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
      const size =
        action.pageSize && action.pageSize > 0
          ? action.pageSize
          : state.range[1] ?? 10;
      return {
        ...state,
        text: action.text,
        textOnChange: action.textOnchange,
        selection: shiftSelection(
          state.textOnChange,
          action.textOnchange,
          state.selection,
        ),
        range: [0, size],
        commitId: state.commitId + 1,
      };
    }
    case "set-text-btn": {
      return {
        ...state,
        textOnChange: action.textOnchange,
        selection: shiftSelection(
          state.textOnChange,
          action.textOnchange,
          state.selection,
        ),
      };
    }
    case "reset-search": {
      return {
        ...state,
        text: "",
        textOnChange: "",
        selection: [],
        range: [0, state.range[1]],
        commitId: state.commitId + 1,
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
            : (state.textOnChange?.slice(
                action.selection.start,
                action.selection.end,
              ) as string);
          const text =
            state.textOnChange?.slice(0, action.selection.start) +
            tokenText +
            state.textOnChange?.slice(action.selection.end);
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
          return { text, selection };
        } else {
          return { text: state.text, selection: action.selection };
        }
      })();

      return {
        ...state,
        text,
        textOnChange: text,
        selection: shiftSelection(
          state.textOnChange || "",
          state.textOnChange || "",
          selection
            ? state.selection.filter((s) => !isOverlapping(s, selection))
            : state.selection,
        ).concat(selection ? [selection] : []),
        range: [0, state.range[1]],
        commitId: state.commitId + 1,
      };
    }
    case "set-filters": {
      return {
        ...state,
        filters: [...state.filters, action.filter],
        range: [0, state.range[1]],
        commitId: state.commitId + 1,
      };
    }
    case "reset-filters": {
      return {
        ...state,
        filters: [],
        range: [0, state.range[1]],
        commitId: state.commitId + 1,
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
        ...state,
        filters,
        range: [0, state.range[1]],
        commitId: state.commitId + 1,
      };
    }
    case "set-range": {
      return { ...state, range: action.range };
    }
    case "set-page-size": {
      const size =
        action.pageSize && action.pageSize > 0
          ? action.pageSize
          : state.range[1];
      return { ...state, range: [0, size], commitId: state.commitId + 1 };
    }
    default:
      return state;
  }
}

function shiftSelection(
  prevText: string,
  nextText: string,
  prevSelection: Array<Selection>,
): Array<Selection> {
  if (prevText === nextText) return prevSelection;
  const commonPrefixLength = findCommonPrefixLength(prevText, nextText);
  const commonSuffixLength = findCommonSuffixLength(
    prevText,
    nextText,
    commonPrefixLength,
  );
  const changePrevEnd = prevText.length - commonSuffixLength;
  const changeNextEnd = nextText.length - commonSuffixLength;
  const changeDelta = changeNextEnd - changePrevEnd;
  const prefixAttributes = prevSelection.filter(
    (a) => a.start <= commonPrefixLength && a.end <= commonPrefixLength,
  );
  const suffixAttributes = prevSelection
    .filter((a) => a.start >= changePrevEnd)
    .map((a) => ({
      ...a,
      start: a.start + changeDelta,
      end: a.end + changeDelta,
    }));
  return prefixAttributes.concat(suffixAttributes);
}

function findCommonPrefixLength(a: string, b: string) {
  const length = Math.min(a.length, b.length);
  let i = 0;
  for (; i < length && a.charCodeAt(i) === b.charCodeAt(i); ++i) {}
  return i;
}

function findCommonSuffixLength(a: string, b: string, startFromIndex: number) {
  const length = Math.min(a.length, b.length) - startFromIndex;
  if (length <= 0) return 0;
  let i = 0;
  for (
    ;
    i < length &&
    a.charCodeAt(a.length - i - 1) === b.charCodeAt(b.length - i - 1);
    ++i
  ) {}
  return i;
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
    case "FILTER":
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
        .every((s) => !isOverlapping(entry, s));
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
