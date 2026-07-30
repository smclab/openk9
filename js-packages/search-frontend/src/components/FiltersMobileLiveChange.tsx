/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import { faChevronDown } from "@fortawesome/free-solid-svg-icons/faChevronDown";
import { faChevronUp } from "@fortawesome/free-solid-svg-icons/faChevronUp";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import capitalize from "lodash/capitalize";
import "overlayscrollbars/css/OverlayScrollbars.css";
import React from "react";
import { useTranslation } from "react-i18next";
import { css } from "styled-components";
import {
  Configuration,
  ConfigurationUpdateFunction,
} from "../embeddable/entry";
import { AddFiltersSvg } from "../svgElement/AddFiltersSvg";
import { FilterSvg } from "../svgElement/FiltersSvg";
import { TrashSvg } from "../svgElement/TrashSvg";
import { SearchToken, SortField } from "./client";
import { DeleteLogo } from "./DeleteLogo";
import { WhoIsDynamic } from "./FilterCategoryDynamic";
import { FiltersMemo } from "./Filters";
import { ModalDetail } from "./ModalDetail";
import { Tab, translationTab } from "./Tabs";
import { useFocusTrap } from "./useFocusTrap";
import { SelectionsAction, SelectionsState } from "./useSelections";
import { useRange } from "./useRange";

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
  haveSearch: boolean | null | undefined;
  callbackClose?: () => void;
  callbackApply?: () => void;
  addExtraClass?: string;
  state: SelectionsState;
};
function FiltersMobileLiveChange<E>({
  dynamicFilters,
  searchQuery,
  sort,
  addExtraClass,
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
  haveSearch,
  callbackClose,
  callbackApply,
  state,
}: FiltersMobileProps<E>) {
  const { t } = useTranslation();
  const [trapFocus] = useFocusTrap(true);
  const { overrideSearchWithCorrection, setOverrideSearchWithCorrection } =
    useRange();
  const componet = (
    <React.Fragment>
      <div
        className={
          "openk9-filter-list-container-first-title container-openk9-filter-mobile-live-change live-change "
        }
        css={css`
          display: flex;
          justify-content: space-beetween;
          background: white;
          justify-content: baseline;
        `}
      >
        <div
          className="openk9-filter-list-container-title box-title live-change-box"
          css={css`
            padding: 0px var(--openk9-embeddable-search--spacing-lg, 16px);
            width: 100%;
            background: white;
            padding-top: var(--openk9-embeddable-search--spacing-xl, 20px);
            padding-bottom: var(--openk9-embeddable-search--spacing-md, 12px);
            display: flex;
          `}
        >
          <div
            className="openk9-filter-list-container-internal-title live-change-title"
            css={css`
              display: flex;
              gap: var(--openk9-embeddable-search--spacing-xs, 4px);
            `}
          >
            <span>
              <FilterSvg />
            </span>
            <span className="openk9-filters-list-title title  live-change-title-span">
              <h2
                css={css`
                  font-style: normal;
                  font-weight: var(
                    --openk9-embeddable-search--font-weight-bold,
                    700
                  );
                  font-size: var(
                    --openk9-embeddable-search--font-size-lg,
                    18px
                  );
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
            font-size: var(--openk9-embeddable-search--font-size-md, 16px);
            font-family: Nunito Sans;
            font-weight: var(--openk9-embeddable-search--font-weight-bold, 700);
            line-height: 12px;
            display: flex;
            align-items: center;
            gap: var(--openk9-embeddable-search--spacing-sm, 8px);
            margin-right: var(--openk9-embeddable-search--spacing-xl, 20px);
            background-color: white;
            border: none;
          `}
          onClick={() => {
            if (setIsVisibleFilters) setIsVisibleFilters(false);
            if (callbackClose) callbackClose();
          }}
        >
          {t("close")} <DeleteLogo heightParam={8} widthParam={8} />
        </button>
      </div>
      <div
        className="openk9-filter-overlay-scrollbars"
        css={css`
          overflow-y: auto;
          height: calc(100vh - 210px);
          border-radius: var(--openk9-embeddable-search--radius-sm, 8px);
        `}
      >
        <FiltersMemo
          state={state}
          memoryResults={memoryResults}
          iconCustom={null}
          searchQuery={searchQuery}
          onAddFilterToken={onAddFilterToken}
          onRemoveFilterToken={onRemoveFilterToken}
          sort={sort}
          isDynamicElement={whoIsDynamic}
          language={language}
          sortAfterKey={sortAfterKey}
          numberOfResults={numberOfResults}
          numberItems={numberItems}
          skeletonCategoryCustom={skeletonCategoryCustom}
          isActiveSkeleton={isActiveSkeleton}
          haveSearch={haveSearch}
          preFilters={
            viewTabs ? (
              <ViewAllTabs
                tabs={tabs}
                onSelectedTabIndexChange={onSelectedTabIndexChange}
                selectedTabIndex={selectedTabIndex}
                language={language}
              />
            ) : null
          }
          overrideSearchWithCorrection={overrideSearchWithCorrection}
          setOverrideSearchWithCorrection={setOverrideSearchWithCorrection}
        />
      </div>

      <footer
        className="openk9-filter-horizontal-container-submit"
        css={css`
          position: fixed;
          bottom: 0;
          left: 0;
          right: 0;
          padding: var(--openk9-embeddable-search--spacing-md, 12px);
          background: white;
          @media (max-width: 480px) {
            padding-inline: var(--openk9-embeddable-search--spacing-xl, 20px);
            flex-direction: column;
          }
          @media (min-width: 481px) and (max-width: 768px) {
            display: flex;
            flex-direction: column;
            gap: var(--openk9-embeddable-search--spacing-xl, 20px);
            width: 180px;
          }
        `}
      >
        <button
          className="openk9-filter-horizontal-submit openk9-filter-button-mobile-remove"
          aria-label={t("remove-filters") || "remove filters"}
          css={css`
            font-size: smaller;
            height: 52px;
            padding: var(--openk9-embeddable-search--spacing-sm, 8px)
              var(--openk9-embeddable-search--spacing-md, 12px);
            white-space: nowrap;
            border: 1px solid
              var(--openk9-embeddable-search--accent-color, #d6012e);
            background-color: var(
              --openk9-embeddable-search--accent-color,
              #d6012e
            );
            border-radius: var(--openk9-embeddable-search--radius-xs, 4px);
            color: white;
            font-weight: var(
              --openk9-embeddable-search--font-weight-semibold,
              600
            );
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: var(--openk9-embeddable-search--spacing-xs, 4px);
            @media (max-width: 480px) {
              background: white;
              border: 1px solid
                var(--openk9-embeddable-search--accent-color, #d6012e);
              width: 100%;
              height: auto;
              margin-top: var(--openk9-embeddable-search--spacing-xl, 20px);
              color: black;
              border-radius: var(
                --openk9-embeddable-search--radius-pill,
                999px
              );
              display: flex;
              justify-content: center;
              color: var(
                --openk9-embeddable-search--secondary-active-color,
                #c0272b
              );
              text-align: center;
              font-size: var(--openk9-embeddable-search--font-size-md, 16px);
              font-style: normal;
              font-weight: var(
                --openk9-embeddable-search--font-weight-bold,
                700
              );
              line-height: normal;
              align-items: center;
            }
          `}
          onClick={() => {
            onConfigurationChange({ filterTokens: [] });
            if (setIsVisibleFilters) setIsVisibleFilters(false);
            callbackClose && callbackClose();
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
            padding: var(--openk9-embeddable-search--spacing-sm, 8px)
              var(--openk9-embeddable-search--spacing-md, 12px);
            white-space: nowrap;
            border: 1px solid
              var(--openk9-embeddable-search--accent-color, #d6012e);
            background-color: var(
              --openk9-embeddable-search--accent-color,
              #d6012e
            );
            border-radius: var(--openk9-embeddable-search--radius-xs, 4px);
            color: white;
            font-weight: var(
              --openk9-embeddable-search--font-weight-semibold,
              600
            );
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: var(--openk9-embeddable-search--spacing-xs, 4px);
            @media (max-width: 480px) {
              background: var(
                --openk9-embeddable-search--accent-color,
                #d6012e
              );
              border: 1px solid
                var(--openk9-embeddable-search--accent-color, #d6012e);
              width: 100%;
              height: auto;
              margin-top: var(--openk9-embeddable-search--spacing-xl, 20px);
              color: white;
              border-radius: var(
                --openk9-embeddable-search--radius-pill,
                999px
              );
              display: flex;
              justify-content: center;
              text-align: center;
              font-size: var(--openk9-embeddable-search--font-size-md, 16px);
              font-style: normal;
              font-weight: var(
                --openk9-embeddable-search--font-weight-bold,
                700
              );
              line-height: normal;
            }
          `}
          onClick={() => {
            if (setIsVisibleFilters) setIsVisibleFilters(false);
            callbackApply && callbackApply();
          }}
        >
          <div>
            {t("result-view")} {numberOfResults}
            {numberOfResults !== 1 ? t("results") : t("result")}
          </div>
          <div>
            <AddFiltersSvg size="21px" />
          </div>
        </button>
      </footer>
    </React.Fragment>
  );
  if (!isVisibleFilters) return null;

  return (
    <div
      className={"modal-detail-container-external " + addExtraClass}
      ref={trapFocus}
    >
      <ModalDetail padding="0px" background="white" content={componet} />
    </div>
  );
}

function ViewAllTabs({
  tabs,
  onSelectedTabIndexChange,
  selectedTabIndex,
  isCollapsable = true,
  language,
}: {
  tabs: Array<Tab>;
  onSelectedTabIndexChange(index: number): void;
  selectedTabIndex: number;
  language: string;
  isCollapsable?: boolean;
}) {
  const [isOpen, setIsOpen] = React.useState(true);
  const { t } = useTranslation();

  return (
    <div
      className="openk9-filter-tabs-container"
      css={css`
        margin-left: var(--openk9-embeddable-search--spacing-lg, 16px);
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
            className={`openk9-mobile-collapsable-filters openk9-collapsable-filters openk9-tabs-in-filters ${
              isOpen
                ? "openk9-dropdown-filters-open"
                : "openk9-dropdown-filters-close"
            }`}
            aria-label={
              t("openk9-collapsable-filter") || "openk9 collapsable filter"
            }
            css={css`
              background: inherit;
              border: none;
            `}
          >
            <FontAwesomeIcon
              icon={isOpen ? faChevronDown : faChevronUp}
              css={css`
                color: var(--openk9-embeddable-search--secondary-text-color);
                font-size: var(--openk9-embeddable-search--font-size-md, 16px);
              `}
            />
          </button>
        )}
      </div>
      <ul
        className="openk9-filter-tabs-list"
        css={css`
          padding: 0px;
          margin: var(--openk9-embeddable-search--spacing-md, 12px) 0px;
        `}
      >
        {isOpen &&
          tabs.map((tab, index) => {
            const tabTraslation = translationTab({
              language: language,
              tabLanguages: tab.translationMap,
              defaultValue: tab.label,
            });
            return (
              <li
                className="openk9-filter-tabs-list-item"
                css={css`
                  display: flex;
                  gap: var(--openk9-embeddable-search--spacing-md, 12px);
                `}
              >
                <div
                  className="openk9-radio"
                  css={css`
                    display: flex;
                    align-items: center;
                    gap: var(--openk9-embeddable-search--spacing-sm, 8px);
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
                      border-radius: var(
                        --openk9-embeddable-search--radius-circle,
                        50%
                      );
                      border: 2px solid #ccc;
                      background-color: ${selectedTabIndex === index
                        ? "var(--openk9-embeddable-search--secondary-active-color) "
                        : "var(--openk9-embeddable-search--primary-background-color, #fff) "};
                      cursor: pointer;
                    `}
                  />
                  <label
                    htmlFor={"tabs" + index}
                    css={css`
                      text-overflow: ellipsis;
                      font-style: normal;
                      font-weight: var(
                        --openk9-embeddable-search--font-weight-semibold,
                        600
                      );
                      line-height: 22px;
                      color: var(
                        --openk9-embeddable-search--primary-text-color,
                        #000000
                      );
                    `}
                  >
                    {capitalize(tabTraslation)}
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

