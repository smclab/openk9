import { SearchToken } from "@openk9/rest-api";
import React from "react";
import { useQuery } from "react-query";
import { css } from "styled-components/macro";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { useOpenK9Client } from "./client";

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
    <div
      css={css`
        position: relative;
        overflow-x: auto;
        height: 35px;
      `}
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
                  ? "var(--openk9-embeddable-search--primary-color)"
                  : ""};
                border-bottom: 2px solid
                  ${isSelected
                    ? "var(--openk9-embeddable-search--active-color)"
                    : "transparent"};
                cursor: pointer;
                font-size: 0.8rem;
                color: var(--openk9-embeddable-search--secondary-text-color);
                user-select: none;
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
    </div>
  );
}
export const TabsMemo = React.memo(Tabs);

export type Tab = { label: string; tokens: Array<SearchToken> };

export function useTenantTabTokens(): Array<Tab> {
  const client = useOpenK9Client();
  const tenantConfiguration = useQuery(
    ["tenant-configuration"] as const,
    ({ queryKey }) => {
      return client.getTenantWithConfiguration();
    },
  );
  const tabTokens = React.useMemo(() => {
    if (tenantConfiguration.data?.config.querySourceBarShortcuts) {
      return [
        {
          label: "All",
          tokens: [],
        },
        ...tenantConfiguration.data.config.querySourceBarShortcuts.map(
          (s): Tab => {
            return {
              label: s.text,
              tokens: [
                {
                  tokenType: "DOCTYPE",
                  keywordKey: "type",
                  values: [s.id],
                  filter: true,
                },
              ],
            };
          },
        ),
      ];
    } else {
      return defaultTabTokens;
    }
  }, [tenantConfiguration.data?.config.querySourceBarShortcuts]);
  return tabTokens;
}
const defaultTabTokens: Array<Tab> = [
  {
    label: "All",
    tokens: [],
  },
  {
    label: "Web",
    tokens: [
      {
        tokenType: "DOCTYPE",
        keywordKey: "type",
        values: ["web"],
        filter: true,
      },
    ],
  },
  {
    label: "Document",
    tokens: [
      {
        tokenType: "DOCTYPE",
        keywordKey: "type",
        values: ["document"],
        filter: true,
      },
    ],
  },
];
