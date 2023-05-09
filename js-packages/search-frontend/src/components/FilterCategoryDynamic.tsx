import React from "react";
import { css } from "styled-components/macro";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown } from "@fortawesome/free-solid-svg-icons/faChevronDown";
import { faChevronUp } from "@fortawesome/free-solid-svg-icons/faChevronUp";
import { faSearch } from "@fortawesome/free-solid-svg-icons/faSearch";
import { SearchToken, SuggestionResult } from "./client";
import { useInfiniteQuery } from "react-query";
import { useDebounce } from "./useDebounce";
import { useOpenK9Client } from "./client";
import { CreateLabel } from "./Filters";
import { PlusSvg } from "../svgElement/PlusSvg";
import { NoFilter, mapSuggestionToSearchToken } from "./FilterCategory";

type FilterCategoryDynamicallyProps = {
  suggestionCategoryId: number;
  suggestionCategoryName: string;
  tokens: SearchToken[];
  onAdd(searchToken: SearchToken): void;
  onRemove(searchToken: SearchToken): void;
  multiSelect: boolean;
  searchQuery: SearchToken[];
  isCollapsable?: boolean;
  isUniqueLoadMore?: boolean;
  loadAll?: boolean;
  setHasMoreSuggestionsCategories?: React.Dispatch<
    React.SetStateAction<boolean>
  >;
};
function FilterCategoryDynamic({
  suggestionCategoryId,
  suggestionCategoryName,
  tokens,
  onAdd,
  onRemove,
  multiSelect,
  searchQuery,
  isCollapsable = true,
  isUniqueLoadMore = false,
  loadAll = false,
  setHasMoreSuggestionsCategories = undefined,
}: FilterCategoryDynamicallyProps) {
  const [text, setText] = React.useState("");
  const suggestions = useInfiniteSuggestions(
    tokens,
    suggestionCategoryId,
    useDebounce(text, 600),
    loadAll,
  );
  const test = suggestions.data?.pages[0].result || [];
  const filters = mergeAndSortObjects(test, searchQuery, suggestionCategoryId);
  React.useEffect(() => {
    if (
      setHasMoreSuggestionsCategories &&
      suggestions &&
      suggestions.hasNextPage
    )
      setHasMoreSuggestionsCategories(suggestions.hasNextPage);
  }, []);

  const [isOpen, setIsOpen] = React.useState(true);
  const [singleSelect, setSingleselect] = React.useState<
    SearchToken | undefined
  >();
  const show = Boolean(text || (filters.length ?? 0) > 0);
  const isMatchFound = searchQuery.some(
    (searchToken) =>
      "goToSuggestion" in searchToken &&
      suggestionCategoryId === searchToken.suggestionCategoryId,
  );

  if (!show)
    return (
      <React.Fragment>
        <NoFilter
          isOpen={isOpen}
          setIsOpen={setIsOpen}
          suggestionCategoryName={suggestionCategoryName}
        />
      </React.Fragment>
    );

  return (
    <div
      className="openk9-filter-category-container"
      css={css`
        margin-bottom: 16px;
        ${isUniqueLoadMore ? "width: 50%" : null}
      `}
    >
      <div>
        <div
          className="openk9-filter-category-title"
          css={css`
            user-select: none;
            margin-left: 16px;
            display: flex;
            align-items: center;
            width: 100% !important;
          `}
          onClick={() => (isCollapsable ? setIsOpen(!isOpen) : null)}
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
          {isCollapsable && (
            <FontAwesomeIcon
              icon={isOpen ? faChevronDown : faChevronUp}
              style={{
                color: "var(--openk9-embeddable-search--secondary-text-color)",
                marginRight: "8px",
              }}
            />
          )}
        </div>
      </div>
      {isOpen && (
        <React.Fragment>
          {!isUniqueLoadMore && (
            <div
              className="openk9-filter-category-container-search"
              css={css`
                display: flex;
                align-items: center;
                margin-bottom: 10px;
              `}
            >
              <FontAwesomeIcon
                icon={faSearch}
                style={{
                  color:
                    "var(--openk9-embeddable-search--secondary-text-color)",
                  marginLeft: "25px",
                  opacity: "0.3",
                  zIndex: "3",
                  marginTop: "16px",
                  height: "15px",
                }}
              />
              <input
                className="openk9-filter-category-search"
                value={text}
                placeholder="Search filters..."
                onChange={(event) => setText(event.currentTarget.value)}
                css={css`
                  margin-top: 17px;
                  flex-grow: 1;
                  text-indent: 25px;
                  margin-left: -25px;
                  margin-right: -9px;
                  padding: 8px 16px 8px 8px;
                  border-radius: 4px;
                  border: 1px solid
                    var(--openk9-embeddable-search--border-color);
                  border-radius: 20px;
                  background: #fafafa;
                  :focus {
                    border: 1px solid
                      var(--openk9-embeddable-search--active-color);
                    outline: none;
                  }
                  ::placeholder {
                    font-style: normal;
                    font-weight: 400;
                    font-size: 15px;
                  }
                `}
              />
            </div>
          )}
          <div
            style={{
              display: "flex",
              flexDirection: isUniqueLoadMore ? "row" : "column",
              gap: isUniqueLoadMore ? "0" : "5px",
              flexWrap: isUniqueLoadMore ? "wrap" : "initial",
              paddingLeft: "13px",
            }}
          >
            {filters.map((suggestion, index) => {
              const asSearchToken = mapSuggestionToSearchToken(
                suggestion,
                true,
              );
              const isChecked = tokens.some((searchToken) => {
                if (
                  JSON.stringify(searchToken.values) ===
                    JSON.stringify(asSearchToken.values) &&
                  searchToken.suggestionCategoryId ===
                    asSearchToken.suggestionCategoryId &&
                  searchToken.keywordKey === asSearchToken.keywordKey
                )
                  return true;
                return false;
              });

              return (
                <React.Fragment>
                  <div
                    key={index}
                    className="form-check"
                    css={css`
                      display: flex;
                      align-items: ${multiSelect ? "baseline" : "stretch"};
                      width: ${isUniqueLoadMore ? "50%" : "auto"};
                      margin-bottom: ${isUniqueLoadMore ? "8px" : "0"};
                    `}
                  >
                    {multiSelect ? (
                      <React.Fragment>
                        <input
                          className="form-check-input"
                          type="checkbox"
                          checked={isChecked}
                          onChange={(event) => {
                            if (event.currentTarget.checked) {
                              if (multiSelect) {
                                onAdd(asSearchToken);
                              } else {
                                tokens.some((searchToken) => {
                                  if (
                                    JSON.parse(JSON.stringify(searchToken))
                                      ?.multiSelect
                                  )
                                    onRemove(searchToken);
                                });
                                onAdd(asSearchToken);
                              }
                            } else {
                              onRemove(asSearchToken);
                            }
                          }}
                          css={css`
                            width: 14px;
                            appearance: none;
                            min-width: 15px;
                            min-height: 15px;
                            border-radius: 4px;
                            border: 2px solid #ccc;
                            background-color: ${isChecked
                              ? "var(--openk9-embeddable-search--secondary-active-color)"
                              : "#fff"};
                            background-size: 100%;
                            background-position: center;
                            background-repeat: no-repeat;
                            cursor: pointer;
                            margin-right: 10px;
                          `}
                        />
                      </React.Fragment>
                    ) : (
                      <SingleSelect
                        isChecked={isChecked}
                        multiSelect={multiSelect}
                        asSearchToken={asSearchToken}
                        onAdd={onAdd}
                        onRemove={onRemove}
                        singleSelect={singleSelect}
                        setSingleSelect={setSingleselect}
                      />
                    )}
                    <span
                      css={css`
                        margin-left: 5px;
                      `}
                    >
                      <label
                        className="form-check-label"
                        css={css`
                          text-overflow: ellipsis;
                          font-style: normal;
                          font-weight: 600;
                          line-height: 22px;
                          /* or 147% */
                          color: #000000;
                        `}
                      >
                        {suggestion.tokenType === "ENTITY" ? (
                          <>
                            <strong
                              className="openk9-filter-category-suggestion-value"
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
                </React.Fragment>
              );
            })}
          </div>
          {!isUniqueLoadMore && suggestions.hasNextPage && (
            <div
              style={{ textAlign: "center", width: "100%", marginTop: "10px" }}
            >
              <CreateLabel
                label=" Load More"
                action={() => {
                  suggestions.fetchNextPage();
                }}
                svgIcon={<PlusSvg size={12} />}
                sizeHeight="22px"
                sizeFont="16px"
                margBottom="18px"
                marginOfSvg="5px"
                marginTop="20px"
              />
            </div>
          )}
        </React.Fragment>
      )}
    </div>
  );
}

export const FilterCategoryDynamicMemo = React.memo(FilterCategoryDynamic);

function useInfiniteSuggestions(
  searchQuery: SearchToken[] | null,
  activeSuggestionCategory: number,
  suggestKeyword: string,
  loadAll: boolean,
) {
  const pageSize = loadAll ? 19 : suggestKeyword === "" ? 7 : 19;
  const client = useOpenK9Client();
  const suggestionCategories = useInfiniteQuery(
    [
      "suggestions",
      searchQuery,
      activeSuggestionCategory,
      suggestKeyword,
      loadAll,
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

  return suggestionCategories;
}

function SingleSelect({
  isChecked,
  multiSelect,
  asSearchToken,
  onAdd,
  onRemove,
  singleSelect,
  setSingleSelect,
}: {
  isChecked: boolean;
  multiSelect: boolean;
  asSearchToken: SearchToken;
  onAdd: (searchToken: SearchToken) => void;
  onRemove: (searchToken: SearchToken) => void;
  singleSelect: SearchToken | undefined;
  setSingleSelect: React.Dispatch<
    React.SetStateAction<SearchToken | undefined>
  >;
}) {
  return (
    <React.Fragment>
      <div>
        <input
          id="radio-button"
          type="radio"
          checked={false}
          onChange={(event) => {
            if (event.currentTarget.checked) {
              if (singleSelect) onRemove(singleSelect);
              setSingleSelect(asSearchToken);
              onAdd(asSearchToken);
            } else {
              onRemove(asSearchToken);
            }
          }}
          onClick={(event) => {
            if (isChecked) {
              onRemove(asSearchToken);
            }
          }}
          style={{
            appearance: "none",
            width: "17px",
            height: "16px",
            borderRadius: "50%",
            border: "2px solid #ccc",
            backgroundColor: isChecked
              ? "var(--openk9-embeddable-search--secondary-active-color)"
              : "#fff",
            cursor: "pointer",
          }}
          onMouseOver={(event) => {
            if (!isChecked) {
              const target = event.target as HTMLInputElement;
              target.style.backgroundColor = "#e6e6e6";
            }
          }}
          onMouseOut={(event) => {
            const target = event.target as HTMLInputElement;
            target.style.backgroundColor = isChecked
              ? "var(--openk9-embeddable-search--secondary-active-color)"
              : "#fff";
          }}
        />
      </div>
    </React.Fragment>
  );
}

//da modificare non appena si deciderà di mettere l'opzionalità (end/or) all'interno di una stessa categoria
function mergeAndSortObjects(
  sortedArray: SuggestionResult[],
  unsortedArray: SearchToken[],
  suggestionCategoryId: number,
): SuggestionResult[] {
  let mergedArray = [...sortedArray];

  if (mergedArray.length === 0) {
    mergedArray = [...unsortedArray]
      .filter(
        (element) =>
          element.tokenType === "TEXT" &&
          "goToSuggestion" in element &&
          element.suggestionCategoryId === suggestionCategoryId,
      )
      .map((element) => ({
        tokenType: "TEXT",
        keywordKey: element.keywordKey,
        value: element.values?.[0] || "",
        suggestionCategoryId: element.suggestionCategoryId || 0,
        count: element.count,
      }));
  }

  for (const element of unsortedArray) {
    const foundElement = mergedArray.find((obj) => {
      if (
        obj.tokenType === "TEXT" &&
        element.tokenType === "TEXT" &&
        element.values[0] !== obj.value &&
        element.suggestionCategoryId === suggestionCategoryId
      ) {
        return (
          obj.tokenType === element.tokenType &&
          (obj.value?.includes(element.values[0]) ||
            obj.value === element.values[0])
        );
      }
    });

    if (foundElement && element.tokenType === "TEXT") {
      const newElement: SuggestionResult = {
        tokenType: "TEXT",
        keywordKey: element.keywordKey,
        value: element.values[0],
        suggestionCategoryId: element.suggestionCategoryId || 0,
        count: element.count,
      };
      mergedArray.push(newElement);
    }
  }

  mergedArray.sort((a, b) => {
    return a.value.localeCompare(b.value);
  });

  return mergedArray;
}