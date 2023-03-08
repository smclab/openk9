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
import { FilterSvg } from "../svgElement/FiltersSvg";
import { PlusSvg } from "../svgElement/PlusSvg";
import { display } from "@mui/system";

type FilterCategoryProps = {
  suggestionCategoryId: number;
  suggestionCategoryName: string;
  tokens: SearchToken[];
  onAdd(searchToken: SearchToken): void;
  onRemove(searchToken: SearchToken): void;
  multiSelect: boolean;
  searchQuery: SearchToken[];
  setCoutFilterSelected: React.Dispatch<
    React.SetStateAction<{
      single: number;
      multiple: number;
    }>
  >;
};
function FilterCategory({
  suggestionCategoryId,
  suggestionCategoryName,
  tokens,
  onAdd,
  onRemove,
  multiSelect,
  setCoutFilterSelected,
  searchQuery,
}: FilterCategoryProps) {
  const [text, setText] = React.useState("");
  const suggestions = useInfiniteSuggestions(
    tokens,
    suggestionCategoryId,
    useDebounce(text, 600),
  );
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
      <NoFilter
        isOpen={isOpen}
        setIsOpen={setIsOpen}
        suggestionCategoryName={suggestionCategoryName}
      />
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
            icon={isOpen ? faChevronDown : faChevronUp}
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
              margin-bottom: 10px;
            `}
          >
            <FontAwesomeIcon
              icon={faSearch}
              style={{
                color: "var(--openk9-embeddable-search--secondary-text-color)",
                marginLeft: "25px",
                opacity: "0.3",
                zIndex: "3",
                marginTop: "16px",
                height: "15px",
              }}
            />
            <input
              value={text}
              placeholder="Cerca contenuti..."
              onChange={(event) => setText(event.currentTarget.value)}
              css={css`
                margin-top: 17px;
                flex-grow: 1;
                text-indent: 25px;
                margin-left: -25px;
                margin-right: -9px;
                padding: 8px 16px 8px 8px;
                border-radius: 4px;
                border: 1px solid var(--openk9-embeddable-search--border-color);
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
          <div
            className="form-check"
            css={css`
              display: flex;
              align-items: "center";
              margin-left: 2px;
              margin-top: 5px;
              flex-direction: column;
            `}
          >
            {searchQuery.map((searchToken: SearchToken, index: number) => {
              if (!("goToSuggestion" in searchToken)) return null;
              if (suggestionCategoryId === searchToken.suggestionCategoryId)
                return (
                  <TokensSelected
                    index={index}
                    multiSelect={multiSelect}
                    onRemove={onRemove}
                    searchToken={searchToken}
                    setCoutFilterSelected={setCoutFilterSelected}
                  />
                );
            })}
          </div>
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
                    <React.Fragment>
                      {isDifferent({
                        singleToken: suggestion.value,
                        tokensSelect: searchQuery,
                      }) && (
                        <div
                          key={index}
                          className="form-check"
                          css={css`
                            display: flex;
                            align-items: ${multiSelect ? "baseline" : "center"};
                            margin-left: 13px;
                            margin-top: 5px;
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
                                      setCoutFilterSelected((countFilter) => ({
                                        ...countFilter,
                                        multiple: countFilter.multiple + 1,
                                      }));
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
                                    setCoutFilterSelected((countFilter) => ({
                                      ...countFilter,
                                      multiple: countFilter.multiple - 1,
                                    }));
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
                              setCoutFilterSelected={setCoutFilterSelected}
                            />
                          )}

                          <span>
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
                      )}
                    </React.Fragment>
                  );
                })}
              </React.Fragment>
            );
          })}
          {suggestions.hasNextPage && (
            <div
              style={{ textAlign: "center", width: "100%", marginTop: "10px" }}
            >
              <CreateLabel
                label=" Mostra Altro"
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

function isDifferent({
  tokensSelect,
  singleToken,
}: {
  tokensSelect: SearchToken[];
  singleToken: string;
}) {
  let result = true;
  tokensSelect.forEach((singleChoice: SearchToken) => {
    if (
      singleChoice.tokenType === "TEXT" &&
      singleChoice?.values[0] === singleToken
    ) {
      result = false;
    }
  });
  return result;
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
  searchQueryParams: SearchToken[] | null,
  activeSuggestionCategory: number,
  suggestKeyword: string,
) {
  const pageSize = 5;
  const client = useOpenK9Client();
  let searchQuery: SearchToken[] | null = [];
  if (searchQueryParams && searchQueryParams?.length > 0) {
    searchQueryParams.forEach((singleSearchQuery) => {
      if (
        singleSearchQuery?.tokenType !== "TEXT" ||
        !("goToSuggestion" in JSON.parse(JSON.stringify(singleSearchQuery)))
      ) {
        searchQuery?.push(singleSearchQuery);
      }
      if (
        singleSearchQuery?.tokenType === "TEXT" &&
        JSON.parse(JSON.stringify(singleSearchQuery)).goToSuggestion
      )
        searchQuery?.push(singleSearchQuery);
    });
  } else {
    searchQuery = searchQueryParams;
  }
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

function SingleSelect({
  isChecked,
  multiSelect,
  asSearchToken,
  onAdd,
  onRemove,
  singleSelect,
  setSingleSelect,
  setCoutFilterSelected,
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
  setCoutFilterSelected: React.Dispatch<
    React.SetStateAction<{
      single: number;
      multiple: number;
    }>
  >;
}) {
  return (
    <React.Fragment>
      <div style={{ display: "flex", alignItems: "center" }}>
        <input
          id="radio-button"
          type="radio"
          checked={isChecked}
          onChange={(event) => {
            if (event.currentTarget.checked) {
              if (singleSelect) onRemove(singleSelect);
              setSingleSelect(asSearchToken);
              onAdd(asSearchToken);
              setCoutFilterSelected((countFilter) => ({
                ...countFilter,
                single: 1,
              }));
            } else {
              onRemove(asSearchToken);
            }
          }}
          onClick={(event) => {
            if (isChecked) {
              onRemove(asSearchToken);
              setCoutFilterSelected((countFilter) => ({
                ...countFilter,
                single: 0,
              }));
            }
          }}
          style={{
            appearance: "none",
            width: "17px",
            height: "16px",
            borderRadius: "50%",
            border: "2px solid #ccc",
            backgroundColor: isChecked
              ? "var(  --openk9-embeddable-search--secondary-active-color)"
              : "#fff",
            marginRight: "10px",
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
        suggestionCategoryId: suggestion.suggestionCategoryId,
      };
    }
  }
};

function NoFilter({
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

function TokensSelected({
  multiSelect,
  onRemove,
  searchToken,
  setCoutFilterSelected,
  index,
}: {
  multiSelect: Boolean;
  onRemove: (searchToken: SearchToken) => void;
  searchToken: {
    tokenType: "TEXT";
    keywordKey?: string | undefined;
    values: string[];
    filter: boolean;
    goToSuggestion?: boolean | undefined;
    label?: string | undefined;
    suggestionCategoryId?: number | undefined;
  };
  setCoutFilterSelected: React.Dispatch<
    React.SetStateAction<{
      single: number;
      multiple: number;
    }>
  >;
  index: number;
}) {
  return (
    <React.Fragment>
      {!multiSelect ? (
        <div
          key={index}
          className="form-check"
          css={css`
            display: flex;
            align-items: ${multiSelect ? "baseline" : "center"};
            margin-left: 12px;
            margin-top: 5px;
          `}
        >
          <input
            className="form-check-input"
            type="checkbox"
            checked={true}
            onChange={(event) => {
              onRemove(searchToken);
              setCoutFilterSelected((countFilter) => ({
                ...countFilter,
                single: countFilter.single - 1,
              }));
            }}
            style={{
              appearance: "none",
              width: "18px",
              height: "17px",
              borderRadius: "50%",
              border: "2px solid #ccc",
              backgroundColor:
                "var(--openk9-embeddable-search--secondary-active-color)",
              marginRight: "10px",
              cursor: "pointer",
            }}
          />
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
            {searchToken.values}
          </label>
        </div>
      ) : (
        <div
          key={index}
          className="form-check"
          css={css`
            display: flex;
            align-items: ${multiSelect ? "baseline" : "center"};
            margin-left: 12px;
            margin-top: 5px;
          `}
        >
          <input
            className="form-check-input"
            type="checkbox"
            checked={true}
            onChange={(event) => {
              onRemove(searchToken);
              setCoutFilterSelected((countFilter) => ({
                ...countFilter,
                multiple: countFilter.multiple - 1,
              }));
            }}
            css={css`
              width: 14px;
              appearance: none;
              min-width: 15px;
              min-height: 15px;
              border-radius: 4px;
              border: 2px solid #ccc;
              background-color: var(
                --openk9-embeddable-search--secondary-active-color
              );
              background-size: 100%;
              background-position: center;
              background-repeat: no-repeat;
              cursor: pointer;
              margin-right: 10px;
            `}
          />
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
            {searchToken.values}
          </label>
        </div>
      )}
    </React.Fragment>
  );
}
