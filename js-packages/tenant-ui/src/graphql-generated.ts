/** Internal type. DO NOT USE DIRECTLY. */
type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
/** Internal type. DO NOT USE DIRECTLY. */
export type Incremental<T> = T | { [P in keyof T]?: P extends ' $fragmentName' | '__typename' ? T[P] : never };
import { gql } from '@apollo/client';
import * as Apollo from '@apollo/client';
const defaultOptions = {} as const;
export enum ApiGroup {
  Administration = 'ADMINISTRATION',
  Ingestion = 'INGESTION',
  Public = 'PUBLIC',
  Search = 'SEARCH'
}

export enum AuthorizationScheme {
  ApiKey = 'API_KEY',
  NoAuth = 'NO_AUTH',
  Oauth2 = 'OAUTH2'
}

export type CreateApiKeyRequestInput = {
  apiGroup: ApiGroup;
  /** ISO-8601 */
  expirationDate?: any;
  name: string;
  tenantName: string;
};

export enum SecurityConfiguration {
  /** No gateway auth, downstream services handle security */
  NoGatewayAuth = 'NO_GATEWAY_AUTH',
  /** OAuth2 for admin only, search and data access are open */
  Oauth2AdminOnly = 'OAUTH2_ADMIN_ONLY',
  /** OAuth2 for admin only, API keys for all other routes */
  Oauth2AdminWithApiKey = 'OAUTH2_ADMIN_WITH_API_KEY',
  /** OAuth2 for admin and search, data access is open */
  Oauth2Search = 'OAUTH2_SEARCH',
  /** OAuth2 for admin and search, API keys for data and ingestion */
  Oauth2SearchWithApiKey = 'OAUTH2_SEARCH_WITH_API_KEY'
}

export type GetApiKeysQueryVariables = Exact<{
  tenantId: string;
}>;


export type GetApiKeysQuery = { apiKeys: Array<{ id: string | null, tenantId: string | null, name: string | null, apiGroup: string | null, status: string | null, prefix: string | null, suffix: string | null, hash: string | null, createDate: any, expirationDate: any } | null> | null };

export type GetApiKeyQueryVariables = Exact<{
  id: string | number;
}>;


export type GetApiKeyQuery = { apiKey: { id: string | null, tenantId: string | null, name: string | null, apiGroup: string | null, status: string | null, prefix: string | null, suffix: string | null, hash: string | null, createDate: any, expirationDate: any } | null };

export type CreateApiKeyMutationVariables = Exact<{
  createApiKeyRequest: CreateApiKeyRequestInput;
}>;


export type CreateApiKeyMutation = { createApiKey: { id: string | null, apiKey: string | null } | null };

export type RevokeApiKeyMutationVariables = Exact<{
  id: string | number;
}>;


export type RevokeApiKeyMutation = { revokeApiKey: boolean | null };

export type TenantQueryVariables = Exact<{
  id: string | number;
}>;


export type TenantQuery = { tenant: { id: string | null, tenantName: string | null, virtualHost: string | null, clientId: string | null, issuerUri: string | null, securityConfiguration: SecurityConfiguration | null, realmProvisioned: boolean } | null };

export type TenantsQueryVariables = Exact<{
  searchText?: string | null | undefined;
  cursor?: string | null | undefined;
}>;


export type TenantsQuery = { tenants: { edges: Array<{ node: { id: string | null, virtualHost: string | null } | null } | null> | null } | null };

export type PreconfigurationsQueryVariables = Exact<{ [key: string]: never; }>;


export type PreconfigurationsQuery = { preconfigurations: Array<{ name: SecurityConfiguration | null, configs: Array<{ apiGroup: ApiGroup | null, authScheme: AuthorizationScheme | null } | null> | null } | null> | null };


export const GetApiKeysDocument = gql`
    query GetApiKeys($tenantId: String!) {
  apiKeys(tenantId: $tenantId) {
    id
    tenantId
    name
    apiGroup
    status
    prefix
    suffix
    hash
    createDate
    expirationDate
  }
}
    `;

/**
 * __useGetApiKeysQuery__
 *
 * To run a query within a React component, call `useGetApiKeysQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetApiKeysQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetApiKeysQuery({
 *   variables: {
 *      tenantId: // value for 'tenantId'
 *   },
 * });
 */
export function useGetApiKeysQuery(baseOptions: Apollo.QueryHookOptions<GetApiKeysQuery, GetApiKeysQueryVariables> & ({ variables: GetApiKeysQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetApiKeysQuery, GetApiKeysQueryVariables>(GetApiKeysDocument, options);
      }
export function useGetApiKeysLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetApiKeysQuery, GetApiKeysQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetApiKeysQuery, GetApiKeysQueryVariables>(GetApiKeysDocument, options);
        }
// @ts-ignore
export function useGetApiKeysSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<GetApiKeysQuery, GetApiKeysQueryVariables>): Apollo.UseSuspenseQueryResult<GetApiKeysQuery, GetApiKeysQueryVariables>;
export function useGetApiKeysSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<GetApiKeysQuery, GetApiKeysQueryVariables>): Apollo.UseSuspenseQueryResult<GetApiKeysQuery | undefined, GetApiKeysQueryVariables>;
export function useGetApiKeysSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<GetApiKeysQuery, GetApiKeysQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<GetApiKeysQuery, GetApiKeysQueryVariables>(GetApiKeysDocument, options);
        }
export type GetApiKeysQueryHookResult = ReturnType<typeof useGetApiKeysQuery>;
export type GetApiKeysLazyQueryHookResult = ReturnType<typeof useGetApiKeysLazyQuery>;
export type GetApiKeysSuspenseQueryHookResult = ReturnType<typeof useGetApiKeysSuspenseQuery>;
export type GetApiKeysQueryResult = Apollo.QueryResult<GetApiKeysQuery, GetApiKeysQueryVariables>;
export const GetApiKeyDocument = gql`
    query GetApiKey($id: ID!) {
  apiKey(id: $id) {
    id
    tenantId
    name
    apiGroup
    status
    prefix
    suffix
    hash
    createDate
    expirationDate
  }
}
    `;

/**
 * __useGetApiKeyQuery__
 *
 * To run a query within a React component, call `useGetApiKeyQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetApiKeyQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetApiKeyQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useGetApiKeyQuery(baseOptions: Apollo.QueryHookOptions<GetApiKeyQuery, GetApiKeyQueryVariables> & ({ variables: GetApiKeyQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetApiKeyQuery, GetApiKeyQueryVariables>(GetApiKeyDocument, options);
      }
export function useGetApiKeyLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetApiKeyQuery, GetApiKeyQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetApiKeyQuery, GetApiKeyQueryVariables>(GetApiKeyDocument, options);
        }
// @ts-ignore
export function useGetApiKeySuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<GetApiKeyQuery, GetApiKeyQueryVariables>): Apollo.UseSuspenseQueryResult<GetApiKeyQuery, GetApiKeyQueryVariables>;
export function useGetApiKeySuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<GetApiKeyQuery, GetApiKeyQueryVariables>): Apollo.UseSuspenseQueryResult<GetApiKeyQuery | undefined, GetApiKeyQueryVariables>;
export function useGetApiKeySuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<GetApiKeyQuery, GetApiKeyQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<GetApiKeyQuery, GetApiKeyQueryVariables>(GetApiKeyDocument, options);
        }
export type GetApiKeyQueryHookResult = ReturnType<typeof useGetApiKeyQuery>;
export type GetApiKeyLazyQueryHookResult = ReturnType<typeof useGetApiKeyLazyQuery>;
export type GetApiKeySuspenseQueryHookResult = ReturnType<typeof useGetApiKeySuspenseQuery>;
export type GetApiKeyQueryResult = Apollo.QueryResult<GetApiKeyQuery, GetApiKeyQueryVariables>;
export const CreateApiKeyDocument = gql`
    mutation CreateApiKey($createApiKeyRequest: CreateApiKeyRequestInput!) {
  createApiKey(createApiKeyRequest: $createApiKeyRequest) {
    id
    apiKey
  }
}
    `;
export type CreateApiKeyMutationFn = Apollo.MutationFunction<CreateApiKeyMutation, CreateApiKeyMutationVariables>;

/**
 * __useCreateApiKeyMutation__
 *
 * To run a mutation, you first call `useCreateApiKeyMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateApiKeyMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createApiKeyMutation, { data, loading, error }] = useCreateApiKeyMutation({
 *   variables: {
 *      createApiKeyRequest: // value for 'createApiKeyRequest'
 *   },
 * });
 */
export function useCreateApiKeyMutation(baseOptions?: Apollo.MutationHookOptions<CreateApiKeyMutation, CreateApiKeyMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateApiKeyMutation, CreateApiKeyMutationVariables>(CreateApiKeyDocument, options);
      }
export type CreateApiKeyMutationHookResult = ReturnType<typeof useCreateApiKeyMutation>;
export type CreateApiKeyMutationResult = Apollo.MutationResult<CreateApiKeyMutation>;
export type CreateApiKeyMutationOptions = Apollo.BaseMutationOptions<CreateApiKeyMutation, CreateApiKeyMutationVariables>;
export const RevokeApiKeyDocument = gql`
    mutation RevokeApiKey($id: ID!) {
  revokeApiKey(id: $id)
}
    `;
export type RevokeApiKeyMutationFn = Apollo.MutationFunction<RevokeApiKeyMutation, RevokeApiKeyMutationVariables>;

/**
 * __useRevokeApiKeyMutation__
 *
 * To run a mutation, you first call `useRevokeApiKeyMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRevokeApiKeyMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [revokeApiKeyMutation, { data, loading, error }] = useRevokeApiKeyMutation({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useRevokeApiKeyMutation(baseOptions?: Apollo.MutationHookOptions<RevokeApiKeyMutation, RevokeApiKeyMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RevokeApiKeyMutation, RevokeApiKeyMutationVariables>(RevokeApiKeyDocument, options);
      }
export type RevokeApiKeyMutationHookResult = ReturnType<typeof useRevokeApiKeyMutation>;
export type RevokeApiKeyMutationResult = Apollo.MutationResult<RevokeApiKeyMutation>;
export type RevokeApiKeyMutationOptions = Apollo.BaseMutationOptions<RevokeApiKeyMutation, RevokeApiKeyMutationVariables>;
export const TenantDocument = gql`
    query Tenant($id: ID!) {
  tenant(id: $id) {
    id
    tenantName
    virtualHost
    clientId
    issuerUri
    securityConfiguration
    realmProvisioned
  }
}
    `;

/**
 * __useTenantQuery__
 *
 * To run a query within a React component, call `useTenantQuery` and pass it any options that fit your needs.
 * When your component renders, `useTenantQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTenantQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useTenantQuery(baseOptions: Apollo.QueryHookOptions<TenantQuery, TenantQueryVariables> & ({ variables: TenantQueryVariables; skip?: boolean; } | { skip: boolean; }) ) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TenantQuery, TenantQueryVariables>(TenantDocument, options);
      }
export function useTenantLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TenantQuery, TenantQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TenantQuery, TenantQueryVariables>(TenantDocument, options);
        }
// @ts-ignore
export function useTenantSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<TenantQuery, TenantQueryVariables>): Apollo.UseSuspenseQueryResult<TenantQuery, TenantQueryVariables>;
export function useTenantSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TenantQuery, TenantQueryVariables>): Apollo.UseSuspenseQueryResult<TenantQuery | undefined, TenantQueryVariables>;
export function useTenantSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TenantQuery, TenantQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<TenantQuery, TenantQueryVariables>(TenantDocument, options);
        }
export type TenantQueryHookResult = ReturnType<typeof useTenantQuery>;
export type TenantLazyQueryHookResult = ReturnType<typeof useTenantLazyQuery>;
export type TenantSuspenseQueryHookResult = ReturnType<typeof useTenantSuspenseQuery>;
export type TenantQueryResult = Apollo.QueryResult<TenantQuery, TenantQueryVariables>;
export const TenantsDocument = gql`
    query Tenants($searchText: String, $cursor: String) {
  tenants(searchText: $searchText, first: 25, after: $cursor) {
    edges {
      node {
        id
        virtualHost
      }
    }
  }
}
    `;

/**
 * __useTenantsQuery__
 *
 * To run a query within a React component, call `useTenantsQuery` and pass it any options that fit your needs.
 * When your component renders, `useTenantsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useTenantsQuery({
 *   variables: {
 *      searchText: // value for 'searchText'
 *      cursor: // value for 'cursor'
 *   },
 * });
 */
export function useTenantsQuery(baseOptions?: Apollo.QueryHookOptions<TenantsQuery, TenantsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TenantsQuery, TenantsQueryVariables>(TenantsDocument, options);
      }
export function useTenantsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TenantsQuery, TenantsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TenantsQuery, TenantsQueryVariables>(TenantsDocument, options);
        }
// @ts-ignore
export function useTenantsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<TenantsQuery, TenantsQueryVariables>): Apollo.UseSuspenseQueryResult<TenantsQuery, TenantsQueryVariables>;
export function useTenantsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TenantsQuery, TenantsQueryVariables>): Apollo.UseSuspenseQueryResult<TenantsQuery | undefined, TenantsQueryVariables>;
export function useTenantsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<TenantsQuery, TenantsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<TenantsQuery, TenantsQueryVariables>(TenantsDocument, options);
        }
export type TenantsQueryHookResult = ReturnType<typeof useTenantsQuery>;
export type TenantsLazyQueryHookResult = ReturnType<typeof useTenantsLazyQuery>;
export type TenantsSuspenseQueryHookResult = ReturnType<typeof useTenantsSuspenseQuery>;
export type TenantsQueryResult = Apollo.QueryResult<TenantsQuery, TenantsQueryVariables>;
export const PreconfigurationsDocument = gql`
    query Preconfigurations {
  preconfigurations {
    name
    configs {
      apiGroup
      authScheme
    }
  }
}
    `;

/**
 * __usePreconfigurationsQuery__
 *
 * To run a query within a React component, call `usePreconfigurationsQuery` and pass it any options that fit your needs.
 * When your component renders, `usePreconfigurationsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = usePreconfigurationsQuery({
 *   variables: {
 *   },
 * });
 */
export function usePreconfigurationsQuery(baseOptions?: Apollo.QueryHookOptions<PreconfigurationsQuery, PreconfigurationsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<PreconfigurationsQuery, PreconfigurationsQueryVariables>(PreconfigurationsDocument, options);
      }
export function usePreconfigurationsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<PreconfigurationsQuery, PreconfigurationsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<PreconfigurationsQuery, PreconfigurationsQueryVariables>(PreconfigurationsDocument, options);
        }
// @ts-ignore
export function usePreconfigurationsSuspenseQuery(baseOptions?: Apollo.SuspenseQueryHookOptions<PreconfigurationsQuery, PreconfigurationsQueryVariables>): Apollo.UseSuspenseQueryResult<PreconfigurationsQuery, PreconfigurationsQueryVariables>;
export function usePreconfigurationsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<PreconfigurationsQuery, PreconfigurationsQueryVariables>): Apollo.UseSuspenseQueryResult<PreconfigurationsQuery | undefined, PreconfigurationsQueryVariables>;
export function usePreconfigurationsSuspenseQuery(baseOptions?: Apollo.SkipToken | Apollo.SuspenseQueryHookOptions<PreconfigurationsQuery, PreconfigurationsQueryVariables>) {
          const options = baseOptions === Apollo.skipToken ? baseOptions : {...defaultOptions, ...baseOptions}
          return Apollo.useSuspenseQuery<PreconfigurationsQuery, PreconfigurationsQueryVariables>(PreconfigurationsDocument, options);
        }
export type PreconfigurationsQueryHookResult = ReturnType<typeof usePreconfigurationsQuery>;
export type PreconfigurationsLazyQueryHookResult = ReturnType<typeof usePreconfigurationsLazyQuery>;
export type PreconfigurationsSuspenseQueryHookResult = ReturnType<typeof usePreconfigurationsSuspenseQuery>;
export type PreconfigurationsQueryResult = Apollo.QueryResult<PreconfigurationsQuery, PreconfigurationsQueryVariables>;