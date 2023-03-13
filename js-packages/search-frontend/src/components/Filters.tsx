import React from "react";
import { css } from "styled-components/macro";
import { SearchToken, SortField } from "./client";
import { buttonStyle, FilterCategoryMemo } from "./FilterCategory";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { useOpenK9Client } from "./client";
import { useQuery } from "react-query";
import { useInfiniteResults } from "./ResultList";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { FilterSvg } from "../svgElement/FiltersSvg";
import { DeleteLogo } from "./DeleteLogo";
import { Logo } from "./Logo";

type FiltersProps = {
  searchQuery: SearchToken[];
  onAddFilterToken(searchToke: SearchToken): void;
  onRemoveFilterToken(searchToken: SearchToken): void;
  onConfigurationChange: ConfigurationUpdateFunction;
  filtersSelect: SearchToken[];
  sort: SortField[];
};
function Filters({
  searchQuery,
  onAddFilterToken,
  onConfigurationChange,
  onRemoveFilterToken,
  filtersSelect,
  sort,
}: FiltersProps) {
  const suggestionCategories = useSuggestionCategories();
  const [lastSearchQueryWithResults, setLastSearchQueryWithResults] =
    React.useState(searchQuery);
  const { data, isPreviousData } = useInfiniteResults(searchQuery, sort);
  React.useEffect(() => {
    if (!isPreviousData) {
      setLastSearchQueryWithResults(searchQuery);
    }
  }, [isPreviousData, searchQuery]);
  const [count, setCount] = React.useState(0);
  React.useEffect(() => {
    const count = searchQuery.filter(
      (search) => "goToSuggestion" in search,
    ).length;
    setCount(count);
  }, [searchQuery]);

  return (
    <OverlayScrollbarsComponent
      style={{
        overflowY: "auto",
        position: "relative",
        height: "100%",
        borderRadius: "8px",
      }}
    >
      <div
        css={css`
          padding: 0px 16px;
          width: 100%;
          background: #fafafa;
          padding-top: 20px;
          padding-bottom: 13px;
          display: flex;
        `}
      >
        <div
          css={css`
            display: flex;
          `}
        >
          <span>
            <FilterSvg />
          </span>
          <span
            css={css`
              margin-left: 10px;
              font-style: normal;
              font-weight: 700;
              font-size: 18px;
              height: 18px;
              line-height: 22px;
              display: flex;
              align-items: center;
              color: #3f3f46;
            `}
          >
            Filters
          </span>
        </div>
      </div>
      <div
        css={css`
          padding: 8px 24px;
        `}
      >
        <div
          css={css`
            display: flex;
            padding: 8px 8px;
            justify-content: space-between;
            border: 1px solid var(--openk9-embeddable-search--border-color);
            border-radius: 8px;
            align-items: center;
            margin-left: -8px;
            width: 100%;
            margin-top: 8px;
          `}
        >
          <div>
            <span
              css={css`
                color: var(--openk9-embeddable-search--active-color);
                font-weight: 700;
              `}
            >
              {count}{" "}
            </span>
            <span>active filters</span>
          </div>
          <div>
            <CreateLabel
              label="remove filters"
              action={() => {
                onConfigurationChange({ filterTokens: [] });
              }}
              svgIconRight={
                <DeleteLogo
                  heightParam={8}
                  widthParam={8}
                  colorSvg={"#C0272B"}
                />
              }
              marginRigthOfSvg={"6px"}
            />
          </div>
        </div>
      </div>

      <div
        css={css`
          position: absolute;
          padding: 16px 16px 0px 0px;
          width: calc(100% - 32px);
        `}
      >
        {suggestionCategories.data?.length === 0 && (
          <div
            css={css`
              color: var(--openk9-embeddable-search--secondary-text-color);
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              height: 50vh;
              margin-left: 30px;
            `}
          >
            <Logo size={100} />
            <h4>No Filters </h4>
          </div>
        )}
        {suggestionCategories.data?.map((suggestionCategory) => {
          return (
            <FilterCategoryMemo
              key={suggestionCategory.id}
              suggestionCategoryName={suggestionCategory.name}
              suggestionCategoryId={suggestionCategory.id}
              tokens={lastSearchQueryWithResults}
              onAdd={onAddFilterToken}
              onRemove={onRemoveFilterToken}
              multiSelect={suggestionCategory?.multiSelect}
              searchQuery={searchQuery}
            />
          );
        })}
      </div>
    </OverlayScrollbarsComponent>
  );
}
export const FiltersMemo = React.memo(Filters);

function useSuggestionCategories() {
  const client = useOpenK9Client();
  return useQuery(
    ["suggestion-categories"],
    async ({ queryKey }) => {
      const result = await client.getSuggestionCategories();
      return result;
    },
    {
      suspense: true,
      keepPreviousData: true,
    },
  );
}

type createLabel = {
  label: string;
  action?(): void;
  svgIcon?: React.ReactNode;
  sizeHeight?: string;
  sizeFont?: string;
  margBottom?: string;
  marginOfSvg?: string;
  marginTop?: string;
  hasBorder?: boolean;
  svgIconRight?: React.ReactNode;
  marginRigthOfSvg?: string;
  colorLabel?: string;
  align?: string;
};
export function CreateLabel({
  label,
  action,
  svgIcon,
  sizeHeight = "15px",
  sizeFont = "12px",
  margBottom = "13px",
  marginOfSvg = "0px",
  marginTop = "0px",
  marginRigthOfSvg = "0px",
  hasBorder = true,
  svgIconRight,
  colorLabel = "var(--openk9-embeddable-search--secondary-active-color)",
  align = "baseline",
}: createLabel) {
  return (
    <div
      css={css`
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 4px 8px;
        gap: 4px;
        height: ${sizeHeight};
        background: #ffffff;
        border: ${hasBorder
          ? "1px solid  var(--openk9-embeddable-search--secondary-active-color);"
          : ""};
        border-radius: 20px;
        margin-left: 10px;
        margin-top: ${marginTop};
        cursor: pointer;
        white-space: nowrap;
      `}
      onClick={action}
    >
      <p
        css={css`
          color: ${colorLabel};
          margin-bottom: ${margBottom};
          font-size: ${sizeFont};
          font-weight: 700;
        `}
      >
        <div
          css={css`
            display: flex;
            align-items: ${align};
          `}
        >
          {svgIcon}
          <span
            css={css`
              margin-left: ${marginOfSvg};
              margin-right: ${marginRigthOfSvg};
            `}
          >
            {label}
          </span>
          {svgIconRight}
        </div>
      </p>
    </div>
  );
}
