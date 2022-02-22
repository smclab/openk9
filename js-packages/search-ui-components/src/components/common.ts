import {
  ALL_SUGGESTION_CATEGORY_ID,
  doSearchEntities,
  getDocumentTypes,
  getSuggestionCategories,
  LoginInfo,
  SearchToken,
  SuggestionResult,
} from "@openk9/rest-api";
import { isEqual } from "lodash";
import React from "react";
import { useQuery } from "react-query";

export const filterSuggestionByActiveSuggestionCategory =
  (activeSuggestionCategoryId: number) => (suggestion: SuggestionResult) =>
    activeSuggestionCategoryId === ALL_SUGGESTION_CATEGORY_ID ||
    suggestion.suggestionCategoryId === activeSuggestionCategoryId;

export const filterSuggestionBySearchQuery =
  (searchQuery: SearchToken[]) => (suggestion: SuggestionResult) => {
    const searchToken = mapSuggestionToSearchToken(suggestion);
    return !searchQuery.some((st) => isEqual(st, searchToken));
  };

export const mapSuggestionToSearchToken = (
  suggestion: SuggestionResult,
): SearchToken => {
  switch (suggestion.tokenType) {
    case "DATASOURCE": {
      return { tokenType: "DATASOURCE", values: [suggestion.value], filter: false };
    }
    case "DOCTYPE": {
      return {
        tokenType: "DOCTYPE",
        keywordKey: "type",
        values: [suggestion.value],
        filter: false
      };
    }
    case "ENTITY": {
      return {
        tokenType: "ENTITY",
        keywordKey: suggestion.keywordKey,
        entityType: suggestion.entityType,
        entityName: suggestion.entityValue,
        values: [suggestion.value],
        filter: false
      };
    }
    case "TEXT": {
      return {
        tokenType: "TEXT",
        keywordKey: suggestion.keywordKey,
        values: [suggestion.value],
        filter: false
      };
    }
  }
};

export function useDocumentTypeSuggestions(text: string) {
  const { data: documentTypes } = useQuery(["document-types"], () => {
    return getDocumentTypes(null);
  });
  const documentTypeSuggestions = React.useMemo(
    () =>
      Object.entries(documentTypes ?? {}).flatMap(
        ([documentType, keywordKeys]) => {
          return keywordKeys.map((keywordKey): SuggestionResult => {
            return {
              tokenType: "TEXT",
              suggestionCategoryId: 0,
              keywordKey,
              value: "",
            };
          });
        },
      ),
    [documentTypes],
  );
  const filteredDocumentTypeSuggestions = text
    ? documentTypeSuggestions.filter((suggestion) => {
        return (
          suggestion.tokenType === "TEXT" &&
          suggestion.keywordKey?.toLowerCase().includes(text.toLowerCase())
        );
      })
    : documentTypeSuggestions;
  return filteredDocumentTypeSuggestions;
}

export function useSuggestionCategories(loginInfo: LoginInfo | null) {
  return useQuery(["suggestion-categories"], async ({ queryKey }) => {
    const result = await getSuggestionCategories(loginInfo);
    return result;
  });
}

export function useEntity(entity: { type: string; id: string } | null) {
  const { type = "", id = "" } = entity || {};
  return useQuery(
    ["entity", type, id] as const,
    async ({ queryKey }) => {
      const [, type, id] = queryKey;
      const found = await doSearchEntities({ type, entityId: id }, null);
      if (found.result.length === 1) return found.result[0];
      else throw new Error();
    },
    { enabled: entity !== null },
  );
}
