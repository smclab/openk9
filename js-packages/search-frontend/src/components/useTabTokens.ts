import React from "react";
import { useQuery } from "react-query";
import { SearchToken } from "@openk9/rest-api";
import { client } from "./client";

type Tab = { label: string; tokens: Array<SearchToken> };

export function useTabTokens(): Array<Tab> {
  const tenantConfiguration = useQuery(
    ["tenant-configuration"] as const,
    ({ queryKey }) => {
      return client.getTentantWithConfiguration();
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
