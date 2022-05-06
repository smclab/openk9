import {
  getTenantWithConfiguration,
  LoginInfo,
  SearchToken,
} from "@openk9/rest-api";
import { useQuery } from "react-query";

export function useTabTokens(
  loginInfo: LoginInfo | null,
): Array<{ label: string; tokens: Array<SearchToken> }> {
  const tenantConfiguration = useQuery(
    ["tenant-configuration"] as const,
    ({ queryKey }) => {
      return getTenantWithConfiguration(loginInfo);
    },
  );
  if (tenantConfiguration.data?.config.querySourceBarShortcuts) {
    return [
      {
        label: "All",
        tokens: [],
      },
      ...tenantConfiguration.data.config.querySourceBarShortcuts.map(
        (s): { label: string; tokens: Array<SearchToken> } => {
          return {
            label: s.text,
            tokens: [
              { tokenType: "DOCTYPE", keywordKey: "type", values: [s.id] },
            ],
          };
        },
      ),
    ];
  } else {
    return defaultTabTokens;
  }
}

const defaultTabTokens: Array<{ label: string; tokens: Array<SearchToken> }> = [
  {
    label: "All",
    tokens: [],
  },
  {
    label: "Web",
    tokens: [{ tokenType: "DOCTYPE", keywordKey: "type", values: ["web"] }],
  },
  {
    label: "Document",
    tokens: [
      { tokenType: "DOCTYPE", keywordKey: "type", values: ["document"] },
    ],
  },
];
