import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown } from "@fortawesome/free-solid-svg-icons/faChevronDown";
import { faChevronUp } from "@fortawesome/free-solid-svg-icons/faChevronUp";
import { faSearch } from "@fortawesome/free-solid-svg-icons/faSearch";
import { SearchToken, SuggestionResult } from "./client";
import isEqual from "lodash/isEqual";
import { useInfiniteQuery } from "react-query";
import { useDebounce } from "./useDebounce";
import { useOpenK9Client } from "./client";
import { Logo } from "./Logo";

type FilterCategoryProps = {
  suggestionCategoryId: number;
  suggestionCategoryName: string;
  tokens: SearchToken[];
  onAdd(searchToken: SearchToken): void;
  onRemove(searchToken: SearchToken): void;
};
function FilterCategory({
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
  if (!show)
    return (
      <div>
        <div>
          <div
            css={css`
              user-select: none;
              margin-left: 16px;
              display: flex;
              align-items: center;
              width: 100% !important;
              margin-bottom: 20px;
            `}
            onClick={() => setIsOpen(!isOpen)}
          >
            <div
              css={css`
                flex-grow: 1;
                :first-letter {
                  text-transform: uppercase;
                }
              `}
            >
              <strong>{suggestionCategoryName}</strong>
            </div>
            <FontAwesomeIcon
              icon={isOpen ? faChevronUp : faChevronDown}
              style={{
                color: "var(--openk9-embeddable-search--secondary-text-color)",
                marginRight: "8px",
              }}
            />
          </div>
        </div>
        {isOpen && (
          <div
            css={css`
              color: var(--openk9-embeddable-search--secondary-text-color);
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              height: 100%;
              margin-top: 30px;
              margin-left: 10px;
            `}
          >
            <Logo size={80} />
            <h3>No {suggestionCategoryName}</h3>
            <div></div>
          </div>
        )}
      </div>
    );
  return (
    <div
      css={css`
        margin-bottom: 16px;
      `}
    >
      <div>
        <div
          css={css`
            user-select: none;
            margin-left: 16px;
            display: flex;
            align-items: center;
            width: 100% !important;
          `}
          onClick={() => setIsOpen(!isOpen)}
        >
          <div
            css={css`
              flex-grow: 1;
              :first-letter {
                text-transform: uppercase;
              }
            `}
          >
            <strong>{suggestionCategoryName}</strong>
          </div>
          <FontAwesomeIcon
            icon={isOpen ? faChevronUp : faChevronDown}
            style={{
              color: "var(--openk9-embeddable-search--secondary-text-color)",
              marginRight: "8px",
            }}
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
                margin-left: 16px;
                margin-right: -31.5px;
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
              style={{
                color: "var(--openk9-embeddable-search--secondary-text-color)",
                marginLeft: "8px",
              }}
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
                      className="form-check"
                      css={css`
                        margin-left: 13px;
                        margin-top: 5px;
                      `}
                    >
                      <input
                        className="form-check-input"
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
                        `}
                      />
                      <span>
                        <label
                          className="form-check-label"
                          css={css`
                            text-overflow: ellipsis;
                          `}
                        >
                          {suggestion.tokenType === "ENTITY" ? (
                            <>
                              <strong
                                css={css`
                                  :first-letter {
                                    text-transform: uppercase;
                                  }
                                  display: inline-block;
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
                      </span>
                    </div>
                  );
                })}
              </React.Fragment>
            );
          })}
          {suggestions.hasNextPage && (
            <div
              style={{ textAlign: "center", width: "100%", marginTop: "10px" }}
            >
              <button
                css={css`
                  width: 163px;
                  height: 32px;
                  left: 75px;
                  top: 730px;
                  background: #ffffff;
                  border: 2px solid #c0272b;
                  border-radius: 12px;
                  cursor: pointer;
                `}
                // disabled={suggestions.isFetching}
                onClick={() => {
                  suggestions.fetchNextPage();
                }}
              >
                <div
                  css={css`
                    display: flex;
                    align-items: center;
                    text-align: center;
                    margin-left: 25px;
                  `}
                >
                  <span
                    css={css`
                      font-family: "Helvetica";
                      font-style: normal;
                      font-weight: 400;
                      font-size: 25px;
                      /* or 176% */
                      color: #c0272b;
                    `}
                  >
                    +
                  </span>
                  <span
                    css={css`
                      font-family: "Helvetica";
                      font-style: normal;
                      color: #c0272b;
                      margin-left: 8px;
                      font-weight: 700;
                      font-size: 14px;
                      color: #c0272b;
                    `}
                  >
                    Load more
                  </span>
                </div>
              </button>
            </div>
          )}
        </React.Fragment>
      )}
    </div>
  );
}
export const FilterCategoryMemo = React.memo(FilterCategory);

export const buttonStyle = css`
  color: inherit;
  font-weight: bold;
  background: none;
  appearance: none;
  font-family: inherit;
  font-size: inherit;
  border: 1px solid var(--openk9-embeddable-search--primary-color);
  color: var(--openk9-embeddable-search--primary-color);
  border-radius: 4px;
  :hover {
    color: var(--openk9-embeddable-search--primary-color);
    cursor: pointer;
  }
  :disabled {
    border: 1px solid var(--openk9-embeddable-search--border-color);
    color: var(--openk9-embeddable-search--border-color);
    cursor: not-allowed;
  }
`;

function useInfiniteSuggestions(
  searchQuery: SearchToken[] | null,
  activeSuggestionCategory: number,
  suggestKeyword: string,
) {
  const pageSize = 10;
  const client = useOpenK9Client();
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
      const result = await client.getSuggestions({
        searchQuery,
        range: [0, pageSize + 1],
        afterKey: pageParam,
        suggestionCategoryId: activeSuggestionCategory,
        suggestKeyword,
        order: suggestKeyword ? "desc" : "asc",
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
        if (!lastPage.afterKey) return undefined;
        if (pages[pages.length - 1].result.length < pageSize) return undefined;
        return lastPage.afterKey;
      },
      suspense: true,
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
        filter: true,
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
