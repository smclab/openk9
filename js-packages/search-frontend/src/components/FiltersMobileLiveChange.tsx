import React from "react";
import { css } from "styled-components/macro";
import {
  GenericResultItem,
  DetailRendererProps,
  SearchToken,
  SortField,
} from "./client";
import { useRenderers } from "./useRenderers";
import "overlayscrollbars/css/OverlayScrollbars.css";
import { ModalDetail } from "./ModalDetail";
import {
  Configuration,
  ConfigurationUpdateFunction,
} from "../embeddable/entry";
import { FilterSvg } from "../svgElement/FiltersSvg";
import { DeleteLogo } from "./DeleteLogo";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { FiltersMemo } from "./Filters";
import { useInfiniteResults } from "./ResultList";
import { TrashSvg } from "../svgElement/TrashSvg";
import { AddFiltersSvg } from "../svgElement/AddFiltersSvg";
import { useTranslation } from "react-i18next";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown } from "@fortawesome/free-solid-svg-icons/faChevronDown";
import { faChevronUp } from "@fortawesome/free-solid-svg-icons/faChevronUp";
import { Tab } from "./Tabs";
import { capitalize } from "lodash";
import { WhoIsDynamic } from "./FilterCategoryDynamic";
import { SelectionsAction } from "./useSelections";

export type FiltersMobileProps<E> = {
  searchQuery: SearchToken[];
  onAddFilterToken: (searchToken: SearchToken) => void;
  onRemoveFilterToken: (searchToken: SearchToken) => void;
  filtersSelect: SearchToken[];
  sort: SortField[];
  dynamicFilters: boolean;
  onConfigurationChange: ConfigurationUpdateFunction;
  configuration: Configuration;
  isVisibleFilters: boolean;
  tabs: Array<Tab>;
  setIsVisibleFilters:
    | React.Dispatch<React.SetStateAction<boolean>>
    | undefined;
  onSelectedTabIndexChange(index: number): void;
  selectedTabIndex: number;
  viewTabs?: boolean;
  language: string;
  sortAfterKey: string;
  isCollapsable?: boolean;
  numberOfResults: number;
  whoIsDynamic: WhoIsDynamic[];
  selectionsDispatch: React.Dispatch<SelectionsAction>;
  numberItems?: number | null | undefined;
  isActiveSkeleton: boolean;
  skeletonCategoryCustom: React.ReactNode | null;
  memoryResults: boolean;
};
function FiltersMobileLiveChange<E>({
  dynamicFilters,
  searchQuery,
  sort,
  onAddFilterToken,
  onRemoveFilterToken,
  onConfigurationChange,
  configuration,
  isVisibleFilters,
  setIsVisibleFilters,
  tabs,
  onSelectedTabIndexChange,
  selectedTabIndex,
  language,
  viewTabs = false,
  sortAfterKey,
  isCollapsable = true,
  numberOfResults,
  whoIsDynamic,
  numberItems,
  isActiveSkeleton,
  skeletonCategoryCustom,
  selectionsDispatch,
  memoryResults,
}: FiltersMobileProps<E>) {
  const results = useInfiniteResults<any>(
    searchQuery,
    sort,
    language,
    sortAfterKey,
    numberOfResults,
    memoryResults,
  );
  const { t } = useTranslation();
  const componet = (
    <React.Fragment>
      <div
        className="openk9-filter-list-container-first-title container-openk9-filter-mobile-live-change live-change"
        css={css`
          display: flex;
          justify-content: space-beetween;
          background: #fafafa;
          justify-content: baseline;
        `}
      >
        <div
          className="openk9-filter-list-container-title box-title live-change-box"
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
            className="openk9-filter-list-container-internal-title live-change-title"
            css={css`
              display: flex;
              gap: 5px;
            `}
          >
            <span>
              <FilterSvg />
            </span>
            <span className="openk9-filters-list-title title  live-change-title-span">
              <h2
                css={css`
                  font-style: normal;
                  font-weight: 700;
                  font-size: 18px;
                  height: 18px;
                  line-height: 22px;
                  display: flex;
                  align-items: center;
                  color: #3f3f46;
                  margin: 0;
                `}
              >
                {t("filters")}
              </h2>
            </span>
          </div>
        </div>
        <button
          className="openk9-filters-list-button-close"
          aria-label={t("close") || "close"}
          css={css`
            color: var(--openk9-grey-stone-600);
            font-size: 15px;
            font-family: Nunito Sans;
            font-weight: 700;
            line-height: 12px;
            display: flex;
            align-items: center;
            gap: 9px;
            margin-right: 21px;
          `}
          onClick={() => {
            if (setIsVisibleFilters) setIsVisibleFilters(false);
          }}
          style={{ backgroundColor: "#FAFAFA", border: "none" }}
        >
          {t("close")} <DeleteLogo heightParam={8} widthParam={8} />
        </button>
      </div>
      <OverlayScrollbarsComponent
        className="openk9-filter-overlay-scrollbars"
        style={{
          overflowY: "auto",
          position: "relative",
          height: "calc(100vh - 210px)",
          borderRadius: "8px",
        }}
      >
        <FiltersMemo
          memoryResults={memoryResults}
          searchQuery={searchQuery}
          onAddFilterToken={onAddFilterToken}
          onRemoveFilterToken={onRemoveFilterToken}
          onConfigurationChange={onConfigurationChange}
          filtersSelect={configuration.filterTokens}
          sort={sort}
          dynamicFilters={dynamicFilters}
          isDynamicElement={whoIsDynamic}
          language={language}
          sortAfterKey={sortAfterKey}
          isCollapsable={isCollapsable}
          numberOfResults={numberOfResults}
          numberItems={numberItems}
          skeletonCategoryCustom={skeletonCategoryCustom}
          isActiveSkeleton={isActiveSkeleton}
          preFilters={
            viewTabs ? (
              <ViewAllTabs
                tabs={tabs}
                onSelectedTabIndexChange={onSelectedTabIndexChange}
                selectedTabIndex={selectedTabIndex}
              />
            ) : null
          }
        />
      </OverlayScrollbarsComponent>

      <footer
        className="openk9-filter-horizontal-container-submit"
        css={css`
          position: fixed;
          bottom: 0;
          left: 0;
          right: 0;
          padding: 10px;
          background: white;
          @media (max-width: 480px) {
            padding-inline: 20px;
            flex-direction: column;
          }
        `}
      >
        <button
          className="openk9-filter-horizontal-submit openk9-filter-button-mobile-remove"
          aria-label={t("remove-filters") || "remove filters"}
          css={css`
            font-size: smaller;
            height: 52px;
            padding: 8px 12px;
            white-space: nowrap;
            border: 1px solid #d6012e;
            background-color: #d6012e;
            border-radius: 5px;
            color: white;
            font-weight: 600;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 3px;
            @media (max-width: 480px) {
              background: white;
              border: 1px solid #d6012e;
              width: 100%;
              height: auto;
              margin-top: 20px;
              color: black;
              border-radius: 50px;
              display: flex;
              justify-content: center;
              color: var(--red-tones-500, #c0272b);
              text-align: center;
              font-size: 16px;
              font-style: normal;
              font-weight: 700;
              line-height: normal;
              align-items: center;
            }
          `}
          onClick={() => {
            onConfigurationChange({ filterTokens: [] });
            if (setIsVisibleFilters) setIsVisibleFilters(false);
            if (selectionsDispatch)
              selectionsDispatch({ type: "reset-filters" });
          }}
        >
          <div>{t("remove-filters")} </div>
          <div>
            <TrashSvg size="18px" />
          </div>
        </button>
        <button
          className="openk9-filter-horizontal-submit openk9-filter-button-mobile-apply"
          aria-label="applica filtri"
          css={css`
            font-size: smaller;
            height: 52px;
            padding: 8px 12px;
            white-space: nowrap;
            border: 1px solid #d6012e;
            background-color: #d6012e;
            border-radius: 5px;
            color: white;
            font-weight: 600;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 3px;
            @media (max-width: 480px) {
              background: #d6012e;
              border: 1px solid #d6012e;
              width: 100%;
              height: auto;
              margin-top: 20px;
              color: white;
              border-radius: 50px;
              display: flex;
              justify-content: center;
              text-align: center;
              font-size: 16px;
              font-style: normal;
              font-weight: 700;
              line-height: normal;
            }
          `}
          onClick={() => {
            if (setIsVisibleFilters) setIsVisibleFilters(false);
          }}
        >
          <div>
            {t("result-view")} {results.data?.pages[0].total}{" "}
            {results.data?.pages[0].total !== 1 ? t("results") : t("result")}
          </div>
          <div>
            <AddFiltersSvg size="21px" />
          </div>
        </button>
      </footer>
    </React.Fragment>
  );
  if (!isVisibleFilters) return null;
  document.body.style.overflow = "hidden";
  return <ModalDetail padding="0px" background="white" content={componet} />;
}

function ViewAllTabs({
  tabs,
  onSelectedTabIndexChange,
  selectedTabIndex,
  isCollapsable = true,
}: {
  tabs: Array<Tab>;
  onSelectedTabIndexChange(index: number): void;
  selectedTabIndex: number;
  isCollapsable?: boolean;
}) {
  const [isOpen, setIsOpen] = React.useState(true);
  const { t } = useTranslation();

  return (
    <div
      className="openk9-filter-tabs-container"
      css={css`
        margin-left: 16px;
      `}
    >
      <div
        className="openk9-filter-category-title"
        css={css`
          user-select: none;
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
          <strong>Tabs</strong>
        </div>
        {isCollapsable && (
          <button
            aria-label={
              t("openk9-collapsable-filter") || "openk9 collapsable filter"
            }
            style={{
              background: "inherit",
              border: "none",
              margin: "1px -9px",
            }}
          >
            <FontAwesomeIcon
              icon={isOpen ? faChevronDown : faChevronUp}
              style={{
                color: "var(--openk9-embeddable-search--secondary-text-color)",
                fontSize: "15px",
              }}
            />
          </button>
        )}
      </div>
      <ul
        className="openk9-filter-tabs-list"
        css={css`
          margin: 10px 0px;
        `}
      >
        {isOpen &&
          tabs.map((tab, index) => {
            return (
              <li
                className="openk9-filter-tabs-list-item"
                css={css`
                  display: flex;
                  gap: 10px;
                `}
              >
                <div
                  className="openk9-radio"
                  css={css`
                    display: flex;
                    align-items: center;
                    gap: 8px;
                  `}
                >
                  <input
                    className={`radio-button ${
                      selectedTabIndex === index
                        ? "filter-category-radio-checked"
                        : "not-checked-filter-category"
                    }`}
                    id={"tabs" + index}
                    type="radio"
                    checked={selectedTabIndex === index}
                    onClick={() => {
                      onSelectedTabIndexChange(index);
                    }}
                    css={css`
                      appearance: none;
                      width: 17px;
                      height: 16px;
                      border-radius: 50%;
                      border: 2px solid #ccc;
                      background-color: ${selectedTabIndex === index
                        ? "var(--openk9-embeddable-search--secondary-active-color) "
                        : "#fff "};
                      cursor: pointer;
                    `}
                  />
                  <label
                    htmlFor={"tabs" + index}
                    css={css`
                      text-overflow: ellipsis;
                      font-style: normal;
                      font-weight: 600;
                      line-height: 22px;
                      color: #000000;
                    `}
                  >
                    {capitalize(tab.label)}
                  </label>
                </div>
              </li>
            );
          })}
      </ul>
    </div>
  );
}

export const FiltersMobileLiveChangeMemo = React.memo(FiltersMobileLiveChange);
