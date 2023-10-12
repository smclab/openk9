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
import { CreateLabel } from "./Filters";
import { PlusSvg } from "../svgElement/PlusSvg";
import { useTranslation } from "react-i18next";
import { ArrowDownSvg } from "../svgElement/ArrowDownSvg";
import { capitalize } from "lodash";

type FilterCategoryProps = {
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
  dynamicFilters: boolean;
  numberItems?: number | null | undefined;
  setHasMoreSuggestionsCategories?: React.Dispatch<
    React.SetStateAction<boolean>
  >;
  language: string;
};
function FilterCategory({
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
  dynamicFilters,
  language,
  numberItems,
  setHasMoreSuggestionsCategories = undefined,
}: FilterCategoryProps) {
  const [text, setText] = React.useState("");
  const suggestions = useInfiniteSuggestions(
    tokens,
    suggestionCategoryId,
    useDebounce(text, 600),
    loadAll,
    dynamicFilters,
    language,
    numberItems,
  );
  const { t } = useTranslation();
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
  const show = Boolean(
    text ||
      (suggestions.data?.pages.flatMap((page) => page.result).length ?? 0) > 0,
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
        @media (max-width: 768px) {
          ${isUniqueLoadMore ? "height: 50%" : null}
        }
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
            <button
              aria-label={
                t("category-collapsable-toggle") ||
                "category collapsable toggle"
              }
              style={{ background: "inherit", border: "none" }}
            >
              <FontAwesomeIcon
                icon={isOpen ? faChevronDown : faChevronUp}
                style={{
                  color:
                    "var(--openk9-embeddable-search--secondary-text-color)",
                  marginRight: "8px",
                }}
              />
            </button>
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
                @media (max-width: 480px) {
                  display: none;
                }
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
              <label
                htmlFor={"search-category-" + suggestionCategoryId}
                className="visually-hidden"
              >
                Search Filter
              </label>
              <input
                className="openk9-filter-category-search"
                type="text"
                id={"search-category-" + suggestionCategoryId}
                value={text}
                placeholder={t("search-filters") || "Search filters..."}
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
            className="openk9-filter-form-check-container"
            css={css`
              display: flex;
              flex-direction: ${isUniqueLoadMore ? "row" : "column"};
              gap: ${isUniqueLoadMore ? "0" : "5px"};
              flex-wrap: ${isUniqueLoadMore ? "wrap" : "initial"};
              padding-left: 13px;
              @media (max-width: 480px) {
                margin-top: 15px;
              }
            `}
          >
            {suggestions.data?.pages.map(({ result }, index) => {
              return (
                <React.Fragment key={index}>
                  {result.map((suggestion, index) => {
                    const asSearchToken = mapSuggestionToSearchToken(
                      suggestion,
                      false,
                    );

                    const isChecked = tokens.some((searchToken) =>
                      isEqual(searchToken, asSearchToken),
                    );
                    return (
                      <React.Fragment key={index}>
                        <div
                          key={index}
                          className="form-check"
                          css={css`
                            display: flex;
                            align-items: ${multiSelect
                              ? "baseline"
                              : "stretch"};
                            width: ${isUniqueLoadMore ? "50%" : "auto"};
                            margin-bottom: ${isUniqueLoadMore ? "8px" : "0"};
                            @media (max-width: 768px) {
                              width: 100%;
                              height: ${isUniqueLoadMore ? "50%" : "auto"};
                            }
                          `}
                        >
                          {multiSelect ? (
                            <React.Fragment>
                              <input
                                className={`form-check-input ${
                                  isChecked
                                    ? "is-checked-fliter-category"
                                    : "not-is-cheched"
                                }`}
                                id={
                                  "" +
                                  index +
                                  suggestion.value.replaceAll(" ", "")
                                }
                                type="checkbox"
                                checked={isChecked}
                                onChange={(event) => {
                                  if (event.currentTarget.checked) {
                                    if (multiSelect) {
                                      onAdd(asSearchToken);
                                    } else {
                                      tokens.some((searchToken) => {
                                        if (
                                          JSON.parse(
                                            JSON.stringify(searchToken),
                                          )?.multiSelect
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
                              index={
                                "" +
                                index +
                                suggestion.value.replaceAll(" ", "")
                              }
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
                              htmlFor={
                                "" +
                                index +
                                suggestion.value.replaceAll(" ", "")
                              }
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
                                capitalize(suggestion.value)
                              )}
                            </label>
                          </span>
                        </div>
                      </React.Fragment>
                    );
                  })}
                </React.Fragment>
              );
            })}
          </div>
          {!isUniqueLoadMore && suggestions.hasNextPage && (
            <div
              className="openk9-container-load-more"
              css={css`
                text-align: center;
                width: 100%;
                display: flex;
                margin-left: 12px;
                margin-top: 10px;
                justify-content: center;
                @media (max-width: 480px) {
                  margin-top: 15px;
                }
              `}
            >
              <button
                className="openk9-load-more-button"
                aria-label={t("load-more-filter") || "load more filters"}
                css={css`
                  background: inherit;
                  color: var(--openk9-embeddable-search--primary-color);
                  font-size: 14px;
                  font-style: normal;
                  font-weight: 400;
                  line-height: normal;
                  display: flex;
                  align-items: center;
                  gap: 10px;
                  cursor: pointer;
                  padding: 8px 16px;
                  border: 1px solid
                    var(--openk9-embeddable-search--primary-color);
                  border-radius: 20px;
                `}
                onClick={() => {
                  suggestions.fetchNextPage();
                }}
              >
                {t("load-more") || "Load More"}
                <ArrowDownSvg size="16px" />
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

export function useInfiniteSuggestions(
  searchQueryParams: SearchToken[] | null,
  activeSuggestionCategory: number,
  suggestKeyword: string,
  loadAll: boolean,
  dynamicFilters: boolean,
  language: string,
  numberItems: number | null | undefined,
) {
  const pageSize = loadAll ? 19 : suggestKeyword === "" ? 8 : 19;
  const NPageSize = numberItems ? numberItems : pageSize;
  const client = useOpenK9Client();
  let searchQuery: SearchToken[] | null = [];
  if (searchQueryParams && searchQueryParams?.length > 0) {
    searchQueryParams.forEach((singleSearchQuery) => {
      if (dynamicFilters) searchQuery?.push(singleSearchQuery);
    });
  } else {
    searchQuery = searchQueryParams;
  }

  const suggestionCategories = useInfiniteQuery(
    [
      "suggestions",
      searchQuery,
      activeSuggestionCategory,
      suggestKeyword,
      loadAll,
      language,
    ] as const,
    async ({
      queryKey: [_, searchQuery, activeSuggestionCategory, suggestKeyword],
      pageParam,
    }) => {
      if (!searchQuery) throw new Error();
      const result = await client.getSuggestions({
        searchQuery,
        range: [0, NPageSize + 1],
        afterKey: pageParam,
        suggestionCategoryId: activeSuggestionCategory,
        suggestKeyword,
        order: suggestKeyword ? "desc" : "asc",
        language: language,
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
  index,
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
  index: string;
}) {
  return (
    <React.Fragment>
      <div>
        <input
          className={`radio-button ${
            isChecked
              ? "filter-category-radio-checked"
              : "not-checked-filter-category"
          }`}
          id={index}
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
          css={css`
            appearance: none !important;
            width: 17px !important;
            height: 16px !important;
            border-radius: 50% !important;
            border: 2px solid #ccc !important;
            background-color: ${isChecked
              ? "var(--openk9-embeddable-search--secondary-active-color) !important"
              : "#fff !important"};
            cursor: pointer !important;
          `}
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
        goToSuggestion: false,
        count: suggestion.count,
        suggestionCategoryId: suggestion.suggestionCategoryId,
      };
    }
  }
};

export function NoFilter({
  setIsOpen,
  isOpen,
  suggestionCategoryName,
}: {
  setIsOpen: React.Dispatch<React.SetStateAction<boolean>>;
  isOpen: Boolean;
  suggestionCategoryName: string;
}) {
  return (
    <div>
      <div>
        <div
          className="openk9-filter-category-no-results-container"
          css={css`
            user-select: none;
            margin-left: 16px;
            display: flex;
            align-items: center;
            width: 100% !important;
            margin-bottom: 16px;
          `}
          onClick={() => setIsOpen(!isOpen)}
        >
          <div
            className="openk9-filter-category-no-results-category-name"
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
            icon={isOpen ? faChevronDown : faChevronUp}
            style={{
              color: "var(--openk9-embeddable-search--secondary-text-color)",
              marginRight: "8px",
            }}
          />
        </div>
      </div>
      {isOpen && (
        <div
          className="openk9-filter-category-no-results-is-open"
          css={css`
            color: var(--openk9-embeddable-search--secondary-text-color);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100%;
            margin-top: 18px;
            margin-left: 10px;
          `}
        >
          <Logo size={100} />
          <h4>No {suggestionCategoryName} </h4>
          <div></div>
        </div>
      )}
    </div>
  );
}
