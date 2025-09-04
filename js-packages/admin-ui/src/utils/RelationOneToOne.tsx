import {
  RagType,
  useDocTypeFieldOptionsAnnotatorsQuery,
  useDocTypeFieldOptionsTokenTabQuery,
  useDocTypeFieldsQuery,
  useDocTypeTemplateListQuery,
  useLanguagesQuery,
  useQueryAnalysesQuery,
  useSearchConfigsQuery,
  useUnboundDocTypeFieldsBySuggestionCategoryQuery,
  useUnboundRagConfigurationsByBucketQuery,
} from "../graphql-generated";

export type Option = { value: string; label: string };
export type UseOptionsResult = {
  options: Option[];
  loading: boolean;
  hasNextPage: boolean;
  loadMore?: () => Promise<void>;
};
export type UseOptionsHook = (searchText: string, extraVariables?: Record<string, any>) => UseOptionsResult;

type ConnectionLike<Node = any> =
  | {
      __typename?: string;
      edges?: Array<{ node?: Node | null } | null> | null;
      pageInfo?: { hasNextPage?: boolean | null; endCursor?: string | null } | null;
    }
  | null
  | undefined;

export function makeUseOptionsHook<
  TData extends Record<string, unknown>,
  TVariables extends Record<string, unknown>,
  TNode = any,
>(p: {
  useQuery: (o: {
    variables: TVariables & { searchText: string; first: number; after: string | null };
    notifyOnNetworkStatusChange?: boolean;
  }) => {
    data?: TData;
    loading?: boolean;
    fetchMore: (o: {
      variables: TVariables & { searchText: string; first: number; after: string | null };
      updateQuery: (prev: TData, ctx: { fetchMoreResult?: TData }) => TData;
    }) => Promise<any>;
  };
  connectionKey: keyof TData;
  toOption?: (n: TNode | undefined | null) => Option;
  first?: number;
}): UseOptionsHook {
  const {
    useQuery,
    connectionKey,
    toOption = (n: any) => ({ value: n?.id ?? "", label: n?.name ?? "" }),
    first = 20,
  } = p;
  return (searchText: string, extraVariables: Record<string, any> = {}) => {
    const { data, loading, fetchMore } = useQuery({
      variables: { searchText, first, after: null, ...extraVariables } as any,
      notifyOnNetworkStatusChange: true,
    });
    const conn = data?.[connectionKey] as ConnectionLike<TNode> | TNode[] | undefined;
    let nodes: TNode[] = [];
    let hasNextPage = false;

    if (Array.isArray(conn)) {
      nodes = conn;
      hasNextPage = false;
    } else if (conn && typeof conn === "object" && "edges" in conn) {
      nodes = (conn.edges ?? []).map((e) => e?.node as TNode).filter(Boolean);
      hasNextPage = !!conn.pageInfo?.hasNextPage;
    }

    const options = nodes.map((n) => toOption(n));
    const loadMore = hasNextPage
      ? async () => {
          await fetchMore({
            variables: {
              searchText,
              first,
              after: (conn as ConnectionLike<TNode>)?.pageInfo?.endCursor ?? null,
              ...extraVariables,
            } as any,
            updateQuery: (prev: any, { fetchMoreResult }: { fetchMoreResult?: any }) => {
              if (!fetchMoreResult) return prev;
              const prevConn = prev?.[connectionKey];
              const nextConn = fetchMoreResult?.[connectionKey];
              if (Array.isArray(prevConn) && Array.isArray(nextConn)) {
                return {
                  ...prev,
                  [connectionKey]: [...prevConn, ...nextConn],
                } as TData;
              } else if (
                prevConn &&
                typeof prevConn === "object" &&
                "edges" in prevConn &&
                nextConn &&
                typeof nextConn === "object" &&
                "edges" in nextConn
              ) {
                return {
                  ...prev,
                  [connectionKey]: {
                    __typename: prevConn?.__typename ?? nextConn?.__typename,
                    edges: [...(prevConn?.edges ?? []), ...(nextConn?.edges ?? [])],
                    pageInfo: nextConn?.pageInfo ?? prevConn?.pageInfo,
                  },
                } as TData;
              }
              return prev;
            },
          });
        }
      : undefined;
    return { options, loading: !!loading, hasNextPage, loadMore };
  };
}

export const useOptionSearchConfig: UseOptionsHook = makeUseOptionsHook({
  useQuery: useSearchConfigsQuery,
  connectionKey: "searchConfigs",
  first: 20,
});
export const useRagConfigurationChatRag: UseOptionsHook = makeUseOptionsHook({
  useQuery: ({ variables, ...rest }) => {
    const { bucketId, ragType } = variables;
    if (!bucketId || !ragType) {
      throw new Error("bucketId and ragType must be provided in extraVariables for useRagConfigurationChatRag");
    }

    return useUnboundRagConfigurationsByBucketQuery({
      variables: {
        bucketId: bucketId as string,
        ragType: ragType as RagType,
      },
      ...rest,
    });
  },
  connectionKey: "unboundRAGConfigurationByBucket",
  first: 20,
});
export const useDocTypeOptions: UseOptionsHook = makeUseOptionsHook({
  useQuery: useDocTypeFieldsQuery,
  connectionKey: "docTypeFields",
  first: 20,
});
export const useQuery: UseOptionsHook = makeUseOptionsHook({
  useQuery: useDocTypeFieldsQuery,
  connectionKey: "docTypeFields",
  first: 20,
});
export const useQueryAnaylyses: UseOptionsHook = makeUseOptionsHook({
  useQuery: useQueryAnalysesQuery,
  connectionKey: "queryAnalyses",
  first: 20,
});
export const useLanguages: UseOptionsHook = makeUseOptionsHook({
  useQuery: useLanguagesQuery,
  connectionKey: "languages",
  first: 20,
});
export const useDocTypesTemplates: UseOptionsHook = makeUseOptionsHook({
  useQuery: useDocTypeTemplateListQuery,
  connectionKey: "docTypeTemplates",
  first: 20,
});
export const useDocTypesAnnotators: UseOptionsHook = makeUseOptionsHook({
  useQuery: useDocTypeFieldOptionsAnnotatorsQuery,
  connectionKey: "options",
  first: 20,
});
export const useDocTypeTokenTab: UseOptionsHook = makeUseOptionsHook({
  useQuery: useDocTypeFieldOptionsTokenTabQuery,
  connectionKey: "options",
  first: 20,
});

export const useDocTypes: UseOptionsHook = makeUseOptionsHook({
  useQuery: ({ variables, ...rest }) => {
    const { suggestionCategoryId } = variables;
    if (suggestionCategoryId === undefined || suggestionCategoryId === null) {
      console.log(suggestionCategoryId);

      throw new Error("SuggestionCategoryId must be provided in extraVariables for useDocTypes ");
    }

    return useUnboundDocTypeFieldsBySuggestionCategoryQuery({
      variables: {
        suggestionCategoryId: suggestionCategoryId as string,
      },
      ...rest,
    });
  },
  connectionKey: "unboundDocTypeFieldsBySuggestionCategory",
  first: 20,
});

export function isValidId(
  input: { id?: string | null | undefined; name?: string | null | undefined } | undefined | null,
) {
  if (!input?.id) return undefined;
  return { id: input.id, name: input.name || input.id };
}
