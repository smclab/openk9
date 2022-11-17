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
      style={{
        position: "relative",
        overflowX: "auto",
        height: "40px",
      }}
    >
      <div
        css={css`
          position: absolute;
          display: flex;
          padding: 0px 16px;
        `}
      >
        {tabs.map((tab, index) => {
          const isSelected = index === selectedTabIndex;
          return (
            <div
              key={index}
              css={css`
                padding: 8px 16px;
                color: ${isSelected
                  ? "var(--openk9-embeddable-search--active-color)"
                  : "var(--openk9-embeddable-search--secondary-text-color)"};
                border-bottom: 2px solid
                  ${isSelected
                    ? "var(--openk9-embeddable-search--active-color)"
                    : "transparent"};
                cursor: pointer;
                user-select: none;
                /* font-weight: ${isSelected ? "bold" : "normal"}; */
              `}
              onClick={() => {
                onSelectedTabIndexChange(index);
                onConfigurationChange({ filterTokens: [] });
              }}
            >
              {tab.label.toUpperCase()}
            </div>
          );
        })}
      </div>
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
