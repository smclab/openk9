import React from "react";
import {
  AnalysisResponseEntry,
  AnalysisToken,
  SearchToken,
  useOpenK9Client,
} from "./client";
import { loadQueryString, saveQueryString } from "./queryString";
import { containsAtLeastOne } from "../embeddable/Main";
import { queryStringMapType, queryStringValues } from "../embeddable/entry";

export function useSelections({
  useKeycloak = true,
  useQueryString = true,
  defaultString = "",
  queryStringValues,
  queryStringMap,
}: {
  useKeycloak?: boolean;
  useQueryString?: boolean;
  defaultString?: string;
  queryStringValues: queryStringValues;
  queryStringMap?: queryStringMapType;
}) {
  const defaultSearch = {
    text: defaultString,
    selection: [],
    textOnChange: defaultString,
    filters: [],
  };

  const remappedQueryStringMap: queryStringMapType = {
    keyObj: queryStringMap?.keyObj,
    text: queryStringValues?.find((k) => k === "text")
      ? queryStringMap?.text
      : undefined,
    textOnChange: queryStringValues?.find((k) => k === "textOnChange")
      ? queryStringMap?.textOnChange
      : undefined,
    selection: queryStringValues?.find((k) => k === "selection")
      ? queryStringMap?.selection
      : undefined,
    filters: queryStringValues?.find((k) => k === "filters")
      ? queryStringMap?.filters
      : undefined,
  };
  const [state, dispatch] = React.useReducer(
    reducer,
    defaultSearch,
    (defaultSearch) =>
      loadQueryString<SelectionsState>(defaultSearch, remappedQueryStringMap),
  );

  const [canSave, setCanSave] = React.useState(false);
  const client = useOpenK9Client();
  React.useEffect(() => {
    if (useKeycloak && client.authInit) {
      client.authInit.then(() => {
        setCanSave(true);
      });
    } else {
      if (!useKeycloak) {
        setCanSave(true);
      }
    }
  }, []);
  React.useEffect(() => {
    if (useKeycloak && canSave && useQueryString) {
      saveQueryString(state, remappedQueryStringMap);
    } else {
      if (!useKeycloak && useQueryString) {
        saveQueryString(state, remappedQueryStringMap);
      }
    }
  }, [canSave, state]);
  return [state, dispatch] as const;
}

export type SelectionsState = {
  text: string;
  selection: Array<Selection>;
  textOnChange: string;
  filters: SearchToken[];
};

export type SelectionsAction =
  | {
      type: "set-text";
      text: string;
      textOnchange: string;
    }
  | {
      type: "set-text-btn";
      textOnchange: string;
    }
  | { type: "reset-search" }
  | {
      type: "set-selection";
      replaceText: boolean;
      selection: Selection;
      textEntity?: string | string | undefined;
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

function reducer(
  state: SelectionsState,
  action: SelectionsAction,
): SelectionsState {
  switch (action.type) {
    case "set-text": {
      return {
        text: action.text,
        filters: state.filters,
        textOnChange: action.textOnchange,
        selection: shiftSelection(
          state.textOnChange,
          action.textOnchange,
          state.selection,
        ),
      };
    }
    case "set-text-btn": {
      return {
        text: state.text,
        filters: state.filters,
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

          return {
            text,
            selection,
          };
        } else {
          return { text: state.text, selection: action.selection };
        }
      })();

      return {
        text: text,
        textOnChange: text,
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
        textOnChange: state.textOnChange,
        filters: [...state.filters, action.filter],
        selection: state.selection,
      };
    }
    case "reset-filters": {
      return {
        text: state.text,
        textOnChange: state.textOnChange,
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
        textOnChange: state.textOnChange,
        selection: state.selection,
        text: state.text,
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
