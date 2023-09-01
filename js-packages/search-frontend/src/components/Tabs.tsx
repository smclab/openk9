import React from "react";
import { useQuery } from "react-query";
import { css } from "styled-components/macro";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { SearchToken } from "./client";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { useOpenK9Client } from "./client";
import _ from "lodash";
const OverlayScrollbarsComponentDockerFix = OverlayScrollbarsComponent as any; // for some reason this component breaks build inside docker

type TabsProps = {
  tabs: Array<Tab>;
  selectedTabIndex: number;
  onSelectedTabIndexChange(index: number): void;
  onConfigurationChange: ConfigurationUpdateFunction;
  language: string;
};
function Tabs({
  tabs,
  selectedTabIndex,
  onSelectedTabIndexChange,
  onConfigurationChange,
  language,
}: TabsProps) {
  return (
    <OverlayScrollbarsComponentDockerFix
      className="openk9-tabs-overlay-scrollbars"
      style={{
        position: "relative",
        overflowX: "auto",
        overflowY: "none",
        height: "60px",
      }}
    >
      <nav
        className="openk9-tabs-container-internal"
        css={css`
          position: absolute;
          display: flex;
          padding-top: 14px;
          padding: 8px 12px;
          width: fit-content;
          height: fit-content;
          gap: 16px;
          @media (max-width: 480px) {
            gap: 10px;
          }
        `}
      >
        {tabs.map((tab, index) => {
          const isSelected = index === selectedTabIndex;
          const tabTraslation = translationTab({
            language: language,
            tabLanguages: tab.translationMap,
            defaultValue: tab.label,
          });
          return (
            <button
              className="openk9-single-tab-container"
              key={index}
              tabIndex={0}
              css={css`
                border: none;
                background: none;
              `}
            >
              <span
                key={index}
                className={
                  "openk9-single-tab " +
                  (isSelected ? "openk9-active-tab" : "openk9-not-active")
                }
                css={css`
                  white-space: nowrap;
                  padding: 8px 12px;
                  background: ${isSelected
                    ? "var(--openk9-embeddable-search--primary-background-tab-color)"
                    : "var(--openk9-embeddable-search--secondary-background-tab-color)"};
                  border-radius: 8px;
                  font: Helvetica Neue LT Std;
                  font-style: normal;
                  display: block;
                  color: ${isSelected
                    ? "var(--openk9-embeddable-search--primary-background-color)"
                    : "var(--openk9-embeddable-tabs--primary-color)"};
                  ${isSelected
                    ? "var(--openk9-embeddable-search--active-color)"
                    : "transparent"};
                  cursor: ${isSelected ? "" : "pointer"};
                  user-select: none;
                  :hover {
                    ${isSelected ? "" : "text-decoration: underline;"}
                  }
                `}
                onClick={() => {
                  onSelectedTabIndexChange(index);
                  onConfigurationChange({ filterTokens: [] });
                }}
              >
                {tabTraslation.toUpperCase()}
              </span>
            </button>
          );
        })}
      </nav>
    </OverlayScrollbarsComponentDockerFix>
  );
}
export const TabsMemo = React.memo(Tabs);

export type Tab = {
  label: string;
  tokens: Array<SearchToken>;
  translationMap: { [key: string]: string };
};

export function useTabTokens(): Array<Tab> {
  const client = useOpenK9Client();
  const tabsByVirtualHostQuery = useQuery(
    ["tabs-by-virtualhost"] as const,
    ({ queryKey }) => {
      return client.getTabsByVirtualHost();
    },
  );
  return tabsByVirtualHostQuery.data ?? [];
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
  const desiredKey = "label." + language;
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
    tabClick = _.cloneDeep(defaultValue);
    tabClick[0] = {
      ...tabClick[0],
      values: [tabLanguages[desiredKey]],
    };
  }

  return tabClick || defaultValue;
}
