import {
  DOCUMENT_TYPES_SUGGESTION_CATEGORY_ID,
  getDocumentTypes,
  SearchQuery,
  SearchToken,
  SuggestionResult,
} from "@openk9/http-api";
import { isEqual } from "lodash";
import React from "react";
import { useQuery } from "react-query";

export const filterSuggestionByActiveSuggestionCategory =
  (activeSuggestionCategoryId: number) => (suggestion: SuggestionResult) =>
    activeSuggestionCategoryId === 0 ||
    suggestion.suggestionCategory === activeSuggestionCategoryId;

export const filterSuggestionBySearchQuery =
  (searchQuery: SearchQuery) => (suggestion: SuggestionResult) => {
    const searchToken = mapSuggestionToSearchToken(suggestion);
    return !searchQuery.some((st) => isEqual(st, searchToken));
  };

export const mapSuggestionToSearchToken = (
  suggestion: SuggestionResult,
): SearchToken => {
  switch (suggestion.tokenType) {
    case "DATASOURCE": {
      return { tokenType: "DATASOURCE", values: [suggestion.value] };
    }
    case "DOCTYPE": {
      return {
        tokenType: "DOCTYPE",
        keywordKey: "type",
        values: [suggestion.value],
      };
    }
    case "ENTITY": {
      return {
        tokenType: "ENTITY",
        keywordKey: suggestion.keywordKey,
        entityType: suggestion.entityType,
        values: [Number(suggestion.value)],
      };
    }
    case "TEXT": {
      return {
        tokenType: "TEXT",
        keywordKey: suggestion.keywordKey,
        values: [suggestion.value],
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
              suggestionCategory: DOCUMENT_TYPES_SUGGESTION_CATEGORY_ID,
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
