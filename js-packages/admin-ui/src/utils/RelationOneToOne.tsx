import { useDocTypeFieldsQuery, useSearchConfigsQuery } from "../graphql-generated";

export type Option = { value: string; label: string };
export type UseOptionsResult = {
  options: Option[];
  loading: boolean;
  hasNextPage: boolean;
  loadMore?: () => Promise<void>;
};
export type UseOptionsHook = (searchText: string) => UseOptionsResult;

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
  return (searchText: string) => {
    const { data, loading, fetchMore } = useQuery({
      variables: { searchText, first, after: null } as any,
      notifyOnNetworkStatusChange: true,
    });
    const conn = (data?.[connectionKey] as ConnectionLike<TNode>) ?? null;
    const options = (conn?.edges ?? []).map((e: any) => toOption(e?.node));
    const hasNextPage = !!conn?.pageInfo?.hasNextPage;
    const loadMore = hasNextPage
      ? async () => {
          await fetchMore({
            variables: { searchText, first, after: conn?.pageInfo?.endCursor ?? null } as any,
            updateQuery: (prev: any, { fetchMoreResult }: { fetchMoreResult?: any }) => {
              if (!fetchMoreResult) return prev;
              const prevConn = prev?.[connectionKey] as ConnectionLike<TNode>;
              const nextConn = fetchMoreResult?.[connectionKey] as ConnectionLike<TNode>;
              return {
                ...prev,
                [connectionKey]: {
                  __typename: prevConn?.__typename ?? nextConn?.__typename,
                  edges: [...(prevConn?.edges ?? []), ...(nextConn?.edges ?? [])],
                  pageInfo: nextConn?.pageInfo ?? prevConn?.pageInfo,
                },
              } as TData;
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
export const useDocTypeOptions: UseOptionsHook = makeUseOptionsHook({
  useQuery: useDocTypeFieldsQuery,
  connectionKey: "docTypeFields",
  first: 20,
});
