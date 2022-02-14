import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faChevronDown,
  faChevronUp,
  faSearch,
} from "@fortawesome/free-solid-svg-icons";
import { myTheme } from "./myTheme";
import {
  getSuggestions,
  SearchToken,
  SuggestionResult,
} from "@openk9/rest-api";
import { isEqual } from "lodash";
import { useInfiniteQuery } from "react-query";
import { useDebounce } from "./useDebounce";

type FilterCategoryProps = {
  suggestionCategoryId: number;
  suggestionCategoryName: string;
  tokens: SearchToken[];
  onAdd(searchToken: SearchToken): void;
  onRemove(searchToken: SearchToken): void;
};
export function FilterCategory({
  suggestionCategoryId,
  suggestionCategoryName,
  tokens,
  onAdd,
  onRemove,
}: FilterCategoryProps) {
  const [text, setText] = React.useState("");
  const suggestions = useInfiniteSuggestions(
    tokens,
    suggestionCategoryId,
    useDebounce(text, 600),
  );
  const [isOpen, setIsOpen] = React.useState(true);
  const show = Boolean(
    text ||
      (suggestions.data?.pages.flatMap((page) => page.result).length ?? 0) > 0,
  );
  if (!show) return null;
  return (
    <div
      css={css`
        margin-bottom: 16px;
      `}
    >
      <div css={css``}>
        <div
          css={css`
            user-select: none;
            display: flex;
            align-items: center;
          `}
          onClick={() => setIsOpen(!isOpen)}
        >
          <div
            css={css`
              flex-grow: 1;
            `}
          >
            <strong>{suggestionCategoryName}</strong>
          </div>
          <FontAwesomeIcon
            icon={isOpen ? faChevronUp : faChevronDown}
            style={{ color: myTheme.grayTexColor, marginRight: "8px" }}
          />
        </div>
      </div>
      {isOpen && (
        <React.Fragment>
          <div
            css={css`
              display: flex;
              align-items: center;
              margin-top: 8px;
              margin-bottom: 8px;
            `}
          >
            <input
              value={text}
              onChange={(event) => setText(event.currentTarget.value)}
              css={css`
                flex-grow: 1;
                margin-right: -24px;
                padding: 8px 16px 8px 8px;
                border-radius: 4px;
                border: 1px solid var(--openk9-embeddable-search--border-color);
                :focus {
                  border: 1px solid
                    var(--openk9-embeddable-search--active-color);
                  outline: none;
                }
              `}
            />
            <FontAwesomeIcon
              icon={faSearch}
              style={{ color: myTheme.grayTexColor, marginRight: "8px" }}
            />
          </div>
          {suggestions.data?.pages.map(({ result }, index) => {
            return (
              <React.Fragment key={index}>
                {result.map((suggestion, index) => {
                  const asSearchToken = mapSuggestionToSearchToken(
                    suggestion,
                    true,
                  );
                  const isChecked = tokens.some((searchToken) =>
                    isEqual(searchToken, asSearchToken),
                  );
                  return (
                    <div
                      key={index}
                      css={css`
                        display: flex;
                      `}
                    >
                      <input
                        type="checkbox"
                        checked={isChecked}
                        onChange={(event) => {
                          if (event.currentTarget.checked) {
                            onAdd(asSearchToken);
                          } else {
                            onRemove(asSearchToken);
                          }
                        }}
                        css={css`
                          width: 14px;
                          margin: 4px;
                          flex-shrink: 0;
                        `}
                      />
                      <label
                        css={css`
                          display: block;
                          :first-letter {
                            text-transform: uppercase;
                          }
                          white-space: nowrap;
                          overflow-x: hidden;
                          text-overflow: ellipsis;
                          :hover {
                            word-break: break-all;
                            white-space: normal;
                          }
                        `}
                      >
                        {suggestion.tokenType === "ENTITY" ? (
                          <>
                            <strong
                              css={css`
                                :first-letter {
                                  text-transform: uppercase;
                                }
                              `}
                            >
                              {suggestion.entityType}
                            </strong>
                            : {suggestion.entityValue}
                          </>
                        ) : (
                          suggestion.value
                        )}
                      </label>
                    </div>
                  );
                })}
              </React.Fragment>
            );
          })}
          {suggestions.hasNextPage && (
            <button
              css={css`
                margin-left: 22px;
                ${buttonAsLinkStyle}
              `}
              disabled={suggestions.isFetching}
              onClick={() => {
                suggestions.fetchNextPage();
              }}
            >
              load more
            </button>
          )}
        </React.Fragment>
      )}
    </div>
  );
}
const buttonAsLinkStyle = css`
  color: -webkit-link;
  cursor: pointer;
  text-decoration: underline;
  background: none;
  appearance: none;
  border: none;
  font-family: inherit;
  padding: 0;
  font-size: inherit;
  text-align: left;
`;

export function useInfiniteSuggestions(
  searchQuery: SearchToken[] | null,
  activeSuggestionCategory: number,
  suggestKeyword: string,
) {
  const ENABLED = true;
  const pageSize = 50;
  return useInfiniteQuery(
    [
      "suggestions",
      searchQuery,
      activeSuggestionCategory,
      suggestKeyword,
    ] as const,
    async ({
      queryKey: [_, searchQuery, activeSuggestionCategory, suggestKeyword],
      pageParam,
    }) => {
      if (!searchQuery) throw new Error();
      const result = await getSuggestions({
        searchQuery,
        range: [0, pageSize],
        afterKey: pageParam,
        loginInfo: null,
        suggestionCategoryId: activeSuggestionCategory,
        suggestKeyword,
      });
      return {
        result: result.result,
        afterKey: result.afterKey,
      };
    },
    {
      enabled: searchQuery !== null,
      keepPreviousData: true,
      getNextPageParam(lastPage, pages) {
        if (ENABLED) {
          if (!lastPage.afterKey) return undefined;
          return lastPage.afterKey;
        } else {
          return undefined;
        }
      },
    },
  );
}

export const mapSuggestionToSearchToken = (
  suggestion: SuggestionResult,
  filter: boolean,
): SearchToken => {
  switch (suggestion.tokenType) {
    case "DATASOURCE": {
      return {
        tokenType: "DATASOURCE",
        values: [suggestion.value],
        filter,
      };
    }
    case "DOCTYPE": {
      return {
        tokenType: "DOCTYPE",
        keywordKey: "type",
        values: [suggestion.value],
        filter,
      };
    }
    case "ENTITY": {
      return {
        tokenType: "ENTITY",
        keywordKey: suggestion.keywordKey,
        entityType: suggestion.entityType,
        entityName: suggestion.entityValue,
        values: [suggestion.value],
        filter,
      };
    }
    case "TEXT": {
      return {
        tokenType: "TEXT",
        keywordKey: suggestion.keywordKey,
        values: [suggestion.value],
        filter,
      };
    }
  }
};
