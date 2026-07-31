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
import React from "react";
import { useQuery } from "react-query";
import { css } from "styled-components";
import { SearchToken } from "./client";
import { useOpenK9Client } from "./client";
import cloneDeep from "lodash/cloneDeep";
import { resetFilterCalendar } from "./DateRangePicker";
import { SelectionsAction } from "./useSelections";
import { Options } from "./SortResults";
import CustomSkeleton from "./Skeleton";
import { TabsCallbackProps } from "../embeddable/entry";

// Underline tabs: the active tab is marked by an accent rule under its label,
// drawn as an inset shadow so selecting a tab never changes the row's metrics.
const tabItemStyle = (isSelected: boolean) => css`
  border: none;
  background: none;
  display: block;
  white-space: nowrap;
  /* the horizontal padding widens the rule beyond the label, so a short tab
     ("test") does not end up with a stub of an underline */
  padding: var(--openk9-embeddable-search--spacing-md, 12px)
    var(--openk9-embeddable-search--spacing-sm, 8px);
  font-size: var(--openk9-embeddable-search--font-size-md, 16px);
  font-weight: ${isSelected
    ? "var(--openk9-embeddable-search--font-weight-semibold, 600)"
    : "var(--openk9-embeddable-search--font-weight-regular, 400)"};
  color: ${isSelected
    ? "var(--openk9-embeddable-search--primary-color, #c22525)"
    : "var(--openk9-embeddable-search--secondary-text-color, #3e4244)"};
  box-shadow: inset 0 -2px 0 ${isSelected ? "var(--openk9-embeddable-search--accent-color, #d6012e)" : "transparent"};
  cursor: ${isSelected ? "default" : "pointer"};
  user-select: none;
  transition: color 120ms ease, box-shadow 120ms ease;
  /* uppercase only the first letter, so tenant labels that carry an internal
     capital (SharePoint) keep it */
  ::first-letter {
    text-transform: uppercase;
  }
  &:hover {
    color: var(--openk9-embeddable-search--primary-color, #c22525);
    box-shadow: inset 0 -2px 0 ${isSelected ? "var(--openk9-embeddable-search--accent-color, #d6012e)" : "var(--openk9-embeddable-search--border-color, #ced4da)"};
  }
`;

type TabsProps = {
  tabs: Array<Tab>;
  selectedTabIndex: number;
  onSelectedTabIndexChange(index: number): void;
  language: string;
  onAction?(): void;
  /**
   * No-ops. The row used to collapse into two arrow buttons when scrollMode was
   * false; it now always scrolls horizontally, so these are only kept so that
   * existing tabsConfigurable objects keep type-checking.
   * @deprecated
   */
  scrollMode?: boolean;
  speed?: number;
  distance?: number;
  step?: number;
  reset?: {
    filters: boolean;
    calendar: boolean;
    sort: boolean;
    search: boolean;
  };
  readMessageScreenReader?: boolean;
  textLabelScreenReader?: string;
  resetFilter: () => void;
  resetSort: () => void;
  selectionsDispatch: React.Dispatch<SelectionsAction>;
  tabsCallbackProps?: TabsCallbackProps;
  isUnselectTab?: boolean;
};
function Tabs({
  tabs,
  selectedTabIndex,
  onSelectedTabIndexChange,
  language,
  onAction,
  reset,
  resetFilter,
  resetSort,
  selectionsDispatch,
  readMessageScreenReader,
  textLabelScreenReader,
  isUnselectTab = false,
  tabsCallbackProps,
}: TabsProps) {
  const submitTab = (index: number) => {
    onSelectedTabIndexChange(index);
    if (reset) {
      if (reset.filters) resetFilter();
      if (reset?.calendar) resetFilterCalendar();
      if (reset?.search)
        selectionsDispatch({
          type: "reset-search",
        });
      if (reset?.sort) resetSort();
    }
    if (onAction) onAction();
  };

  // with no tabs there is nothing to switch between, and rendering the row
  // anyway would leave its hairline floating on its own
  if (tabs.length === 0 && !tabsCallbackProps) return null;

  return (
    <React.Fragment>
      <h2
        id="title-tabs-openk9"
        className="visually-hidden"
        css={css`
          border: 0;
          padding: 0;
          margin: 0;
          position: absolute !important;
          height: 1px;
          width: 1px;
          overflow: hidden;
          clip: rect(
            1px 1px 1px 1px
          ); /* IE6, IE7 - a 0 height clip, off to the bottom right of the visible 1px box */
          clip: rect(
            1px,
            1px,
            1px,
            1px
          ); /*maybe deprecated but we need to support legacy browsers */
          clip-path: inset(50%);
          white-space: nowrap;
        `}
      >
        {textLabelScreenReader || "filtri e argomenti"}
      </h2>
      <ul
        className="openk9-tabs-container-internal"
        role="list"
        css={css`
          display: flex;
          align-items: flex-end;
          width: 100%;
          box-sizing: border-box;
          /* indented past the section header above it (which sits at 20px) so
             the row reads as nested under the count, while its hairline still
             runs the full width of the panel */
          padding: 0 var(--openk9-embeddable-search--spacing-2xl, 24px);
          height: fit-content;
          gap: var(--openk9-embeddable-search--spacing-xl, 20px);
          margin: 0;
          list-style-type: none;
          overflow-x: auto;
          scrollbar-width: none;
          ::-webkit-scrollbar {
            display: none;
          }
          @media (max-width: 480px) {
            gap: var(--openk9-embeddable-search--spacing-md, 12px);
          }
        `}
      >
        {tabsCallbackProps
          ? tabsCallbackProps({
              callbackSelectTab: ({ index }) => {
                submitTab(index);
              },
              callbackUnselectTab: (index: number) => {
                submitTab(index);
              },
              tabs,
              selectedTabIndex: selectedTabIndex,
            })
          : tabs.map((tab, index) => {
              const isSelected = index === selectedTabIndex;
              const tabTraslation = translationTab({
                language: language,
                tabLanguages: tab.translationMap,
                defaultValue: tab.label,
              });
              return (
                <li
                  role="listitem"
                  aria-labelledby="title-tabs-openk9"
                  key={"tabs" + index}
                >
                  <button
                    className={`openk9-single-tab-container openk9-single-tab ${
                      isSelected ? "openk9-active-tab" : "openk9-not-active"
                    }`}
                    key={index}
                    tabIndex={0}
                    css={tabItemStyle(isSelected)}
                    onClick={() => {
                      isSelected
                        ? isUnselectTab && submitTab(-1)
                        : submitTab(index);
                    }}
                  >
                    {tabTraslation}
                  </button>
                </li>
              );
            })}
      </ul>
    </React.Fragment>
  );
}
export const TabsMemo = React.memo(Tabs);

export type Tab = {
  label: string;
  tokens: Array<SearchToken>;
  translationMap: { [key: string]: string };
  sortings: Options;
};

export function useTabTokens(): { tab: Array<Tab>; isLoading: boolean } {
  const client = useOpenK9Client();
  const tabsByVirtualHostQuery = useQuery(
    ["tabs-by-virtualhost"] as const,
    ({ queryKey }) => {
      return client.getTabsByVirtualHost();
    },
  );
  return {
    tab: tabsByVirtualHostQuery.data ?? [],
    isLoading: tabsByVirtualHostQuery.isLoading,
  };
}

export function translationTab({
  language,
  tabLanguages,
  defaultValue,
}: {
  language: string;
  tabLanguages: { [key: string]: string };
  defaultValue: string;
}): string {
  const desiredKey = "name." + language;
  if (tabLanguages && tabLanguages.hasOwnProperty(desiredKey)) {
    return tabLanguages[desiredKey];
  }
  return defaultValue;
}

//utile nel caso bisogna tradurre i tab da inviare nella search query
export function translationTabValue({
  language,
  tabLanguages,
  defaultValue,
  selectedTabIndex,
}: {
  language: string;
  tabLanguages: { [key: string]: string };
  defaultValue: SearchToken[];
  selectedTabIndex: number;
}): SearchToken[] {
  if (selectedTabIndex === 0) return defaultValue;
  const desiredKey = "label." + language;
  let tabClick: SearchToken[] | null = null;
  if (tabLanguages && tabLanguages.hasOwnProperty(desiredKey)) {
    tabClick = cloneDeep(defaultValue);
    tabClick[0] = {
      ...tabClick[0],
      values: [tabLanguages[desiredKey]],
    };
  }

  return tabClick || defaultValue;
}

export default function TabsSkeleton() {
  return (
    <div
      css={css`
        padding: var(--openk9-embeddable-search--spacing-sm, 8px)
          var(--openk9-embeddable-search--spacing-lg, 16px);
      `}
    >
      <CustomSkeleton
        height="32px"
        counter={3}
        position="row"
        width="100px"
        gap="10px"
      />
    </div>
  );
}
