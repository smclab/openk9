import React from "react";
import { useQuery } from "react-query";
import { css } from "styled-components/macro";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { SearchToken } from "./client";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { useOpenK9Client } from "./client";
const OverlayScrollbarsComponentDockerFix = OverlayScrollbarsComponent as any; // for some reason this component breaks build inside docker

type TabsProps = {
  tabs: Array<Tab>;
  selectedTabIndex: number;
  onSelectedTabIndexChange(index: number): void;
  onConfigurationChange: ConfigurationUpdateFunction;
};
function Tabs({
  tabs,
  selectedTabIndex,
  onSelectedTabIndexChange,
  onConfigurationChange,
}: TabsProps) {
  return (
    <OverlayScrollbarsComponentDockerFix
      className="openk9-tabs-overlay-scrollbars"
      style={{
        position: "relative",
        overflowX: "auto",
        overflowY: "none",
        height: "50px",
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
          gap: 20px;
          @media (max-width: 480px) {
            gap: 10px;
          }
        `}
      >
        {tabs.map((tab, index) => {
          const isSelected = index === selectedTabIndex;
          return (
            <button
              className="openk9-single-tab-container"
              key={index}
              tabIndex={0}
              css={css`
                border: none;
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
                {tab.label.toUpperCase()}
              </span>
            </button>
          );
        })}
      </nav>
    </OverlayScrollbarsComponentDockerFix>
  );
}
export const TabsMemo = React.memo(Tabs);

export type Tab = { label: string; tokens: Array<SearchToken> };

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
