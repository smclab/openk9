import { gql } from '@apollo/client';
import * as Apollo from '@apollo/client';
export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
const defaultOptions = {} as const;
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
  BigDecimal: any;
  BigInteger: any;
  DateTime: any;
};

export enum ApiGroup {
  Administration = 'ADMINISTRATION',
  Ingestion = 'INGESTION',
  Public = 'PUBLIC',
  Search = 'SEARCH'
}

export type ApiKeyResponse = {
  __typename?: 'ApiKeyResponse';
  apiGroup?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  /** ISO-8601 */
  expirationDate?: Maybe<Scalars['DateTime']>;
  hash?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['String']>;
  name?: Maybe<Scalars['String']>;
  prefix?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
  suffix?: Maybe<Scalars['String']>;
  tenantId?: Maybe<Scalars['String']>;
};

export enum AuthorizationScheme {
  ApiKey = 'API_KEY',
  NoAuth = 'NO_AUTH',
  Oauth2 = 'OAUTH2'
}

export type Config = {
  __typename?: 'Config';
  apiGroup?: Maybe<ApiGroup>;
  authScheme?: Maybe<AuthorizationScheme>;
};

/** A connection to a list of items. */
export type Connection_TenantResponseDto = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_TenantResponseDto>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

export type CreateApiKeyRequestInput = {
  apiGroup: ApiGroup;
  /** ISO-8601 */
  expirationDate?: InputMaybe<Scalars['DateTime']>;
  name: Scalars['String'];
  tenantName: Scalars['String'];
};

export type CreateApiKeyResponse = {
  __typename?: 'CreateApiKeyResponse';
  apiKey?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['String']>;
};

export type DefaultConnection_TenantResponseDto = Connection_TenantResponseDto & {
  __typename?: 'DefaultConnection_TenantResponseDTO';
  edges?: Maybe<Array<Maybe<Edge_TenantResponseDto>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultEdge_TenantResponseDto = Edge_TenantResponseDto & {
  __typename?: 'DefaultEdge_TenantResponseDTO';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<TenantResponseDto>;
};

export type DefaultPageInfo = PageInfo & {
  __typename?: 'DefaultPageInfo';
  endCursor?: Maybe<Scalars['String']>;
  hasNextPage: Scalars['Boolean'];
  hasPreviousPage: Scalars['Boolean'];
  startCursor?: Maybe<Scalars['String']>;
};

export enum Direction {
  Asc = 'ASC',
  Desc = 'DESC'
}

/** An edge in a connection */
export type Edge_TenantResponseDto = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<TenantResponseDto>;
};

export type FieldValidator = {
  __typename?: 'FieldValidator';
  field?: Maybe<Scalars['String']>;
  message?: Maybe<Scalars['String']>;
};

/** Mutation root */
export type Mutation = {
  __typename?: 'Mutation';
  createApiKey?: Maybe<CreateApiKeyResponse>;
  revokeApiKey?: Maybe<Scalars['Boolean']>;
  tenant?: Maybe<Response_TenantResponseDto>;
};


/** Mutation root */
export type MutationCreateApiKeyArgs = {
  createApiKeyRequest: CreateApiKeyRequestInput;
};


/** Mutation root */
export type MutationRevokeApiKeyArgs = {
  id: Scalars['ID'];
};


/** Mutation root */
export type MutationTenantArgs = {
  tenantRequestDTO?: InputMaybe<TenantRequestDtoInput>;
};

/** Information about pagination in a connection. */
export type PageInfo = {
  /** When paginating forwards, the cursor to continue. */
  endCursor?: Maybe<Scalars['String']>;
  /** When paginating forwards, are there more items? */
  hasNextPage: Scalars['Boolean'];
  /** When paginating backwards, are there more items? */
  hasPreviousPage: Scalars['Boolean'];
  /** When paginating backwards, the cursor to continue. */
  startCursor?: Maybe<Scalars['String']>;
};

export type Preconfiguration = {
  __typename?: 'Preconfiguration';
  configs?: Maybe<Array<Maybe<Config>>>;
  name?: Maybe<SecurityConfiguration>;
};

/** Query root */
export type Query = {
  __typename?: 'Query';
  apiKey?: Maybe<ApiKeyResponse>;
  apiKeys?: Maybe<Array<Maybe<ApiKeyResponse>>>;
  preconfigurations?: Maybe<Array<Maybe<Preconfiguration>>>;
  tenant?: Maybe<TenantResponseDto>;
  tenants?: Maybe<Connection_TenantResponseDto>;
};


/** Query root */
export type QueryApiKeyArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryApiKeysArgs = {
  tenantId: Scalars['String'];
};


/** Query root */
export type QueryTenantArgs = {
  id?: InputMaybe<Scalars['ID']>;
};


/** Query root */
export type QueryTenantsArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};

export type Response_TenantResponseDto = {
  __typename?: 'Response_TenantResponseDTO';
  entity?: Maybe<TenantResponseDto>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
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

export type SortByInput = {
  column?: InputMaybe<Scalars['String']>;
  direction?: InputMaybe<Direction>;
};

export type TenantRequestDtoInput = {
  clientId: Scalars['String'];
  clientSecret?: InputMaybe<Scalars['String']>;
  issuerUri: Scalars['String'];
  securityConfiguration: SecurityConfiguration;
  tenantName: Scalars['String'];
  virtualHost: Scalars['String'];
};

export type TenantResponseDto = {
  __typename?: 'TenantResponseDTO';
  clientId?: Maybe<Scalars['String']>;
  clientSecret?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['String']>;
  issuerUri?: Maybe<Scalars['String']>;
  realmProvisioned: Scalars['Boolean'];
  securityConfiguration?: Maybe<SecurityConfiguration>;
  tenantName?: Maybe<Scalars['String']>;
  virtualHost?: Maybe<Scalars['String']>;
};

export type GetApiKeysQueryVariables = Exact<{
  tenantId: Scalars['String'];
}>;


export type GetApiKeysQuery = { __typename?: 'Query', apiKeys?: Array<{ __typename?: 'ApiKeyResponse', id?: string | null, tenantId?: string | null, name?: string | null, apiGroup?: string | null, status?: string | null, prefix?: string | null, suffix?: string | null, hash?: string | null, createDate?: any | null, expirationDate?: any | null } | null> | null };

export type GetApiKeyQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type GetApiKeyQuery = { __typename?: 'Query', apiKey?: { __typename?: 'ApiKeyResponse', id?: string | null, tenantId?: string | null, name?: string | null, apiGroup?: string | null, status?: string | null, prefix?: string | null, suffix?: string | null, hash?: string | null, createDate?: any | null, expirationDate?: any | null } | null };

export type CreateApiKeyMutationVariables = Exact<{
  createApiKeyRequest: CreateApiKeyRequestInput;
}>;


export type CreateApiKeyMutation = { __typename?: 'Mutation', createApiKey?: { __typename?: 'CreateApiKeyResponse', id?: string | null, apiKey?: string | null } | null };

export type RevokeApiKeyMutationVariables = Exact<{
  id: Scalars['ID'];
}>;


export type RevokeApiKeyMutation = { __typename?: 'Mutation', revokeApiKey?: boolean | null };

export type TenantQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type TenantQuery = { __typename?: 'Query', tenant?: { __typename?: 'TenantResponseDTO', id?: string | null, tenantName?: string | null, virtualHost?: string | null, clientId?: string | null, clientSecret?: string | null, issuerUri?: string | null, securityConfiguration?: SecurityConfiguration | null, realmProvisioned: boolean } | null };

export type TenantsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type TenantsQuery = { __typename?: 'Query', tenants?: { __typename?: 'DefaultConnection_TenantResponseDTO', edges?: Array<{ __typename?: 'DefaultEdge_TenantResponseDTO', node?: { __typename?: 'TenantResponseDTO', id?: string | null, virtualHost?: string | null } | null } | null> | null } | null };

export type PreconfigurationsQueryVariables = Exact<{ [key: string]: never; }>;


export type PreconfigurationsQuery = { __typename?: 'Query', preconfigurations?: Array<{ __typename?: 'Preconfiguration', name?: SecurityConfiguration | null, configs?: Array<{ __typename?: 'Config', apiGroup?: ApiGroup | null, authScheme?: AuthorizationScheme | null } | null> | null } | null> | null };

export type CreateTenantMutationVariables = Exact<{
  tenantRequestDTO: TenantRequestDtoInput;
}>;


export type CreateTenantMutation = { __typename?: 'Mutation', tenant?: { __typename?: 'Response_TenantResponseDTO', entity?: { __typename?: 'TenantResponseDTO', id?: string | null, tenantName?: string | null, virtualHost?: string | null, securityConfiguration?: SecurityConfiguration | null } | null, fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };


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
export function useGetApiKeysQuery(baseOptions: Apollo.QueryHookOptions<GetApiKeysQuery, GetApiKeysQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetApiKeysQuery, GetApiKeysQueryVariables>(GetApiKeysDocument, options);
      }
export function useGetApiKeysLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetApiKeysQuery, GetApiKeysQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetApiKeysQuery, GetApiKeysQueryVariables>(GetApiKeysDocument, options);
        }
export type GetApiKeysQueryHookResult = ReturnType<typeof useGetApiKeysQuery>;
export type GetApiKeysLazyQueryHookResult = ReturnType<typeof useGetApiKeysLazyQuery>;
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
export function useGetApiKeyQuery(baseOptions: Apollo.QueryHookOptions<GetApiKeyQuery, GetApiKeyQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetApiKeyQuery, GetApiKeyQueryVariables>(GetApiKeyDocument, options);
      }
export function useGetApiKeyLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetApiKeyQuery, GetApiKeyQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetApiKeyQuery, GetApiKeyQueryVariables>(GetApiKeyDocument, options);
        }
export type GetApiKeyQueryHookResult = ReturnType<typeof useGetApiKeyQuery>;
export type GetApiKeyLazyQueryHookResult = ReturnType<typeof useGetApiKeyLazyQuery>;
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
    clientSecret
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
export function useTenantQuery(baseOptions: Apollo.QueryHookOptions<TenantQuery, TenantQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<TenantQuery, TenantQueryVariables>(TenantDocument, options);
      }
export function useTenantLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<TenantQuery, TenantQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<TenantQuery, TenantQueryVariables>(TenantDocument, options);
        }
export type TenantQueryHookResult = ReturnType<typeof useTenantQuery>;
export type TenantLazyQueryHookResult = ReturnType<typeof useTenantLazyQuery>;
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
export type TenantsQueryHookResult = ReturnType<typeof useTenantsQuery>;
export type TenantsLazyQueryHookResult = ReturnType<typeof useTenantsLazyQuery>;
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
export type PreconfigurationsQueryHookResult = ReturnType<typeof usePreconfigurationsQuery>;
export type PreconfigurationsLazyQueryHookResult = ReturnType<typeof usePreconfigurationsLazyQuery>;
export type PreconfigurationsQueryResult = Apollo.QueryResult<PreconfigurationsQuery, PreconfigurationsQueryVariables>;
export const CreateTenantDocument = gql`
    mutation CreateTenant($tenantRequestDTO: TenantRequestDTOInput!) {
  tenant(tenantRequestDTO: $tenantRequestDTO) {
    entity {
      id
      tenantName
      virtualHost
      securityConfiguration
    }
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateTenantMutationFn = Apollo.MutationFunction<CreateTenantMutation, CreateTenantMutationVariables>;

/**
 * __useCreateTenantMutation__
 *
 * To run a mutation, you first call `useCreateTenantMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateTenantMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createTenantMutation, { data, loading, error }] = useCreateTenantMutation({
 *   variables: {
 *      tenantRequestDTO: // value for 'tenantRequestDTO'
 *   },
 * });
 */
export function useCreateTenantMutation(baseOptions?: Apollo.MutationHookOptions<CreateTenantMutation, CreateTenantMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateTenantMutation, CreateTenantMutationVariables>(CreateTenantDocument, options);
      }
export type CreateTenantMutationHookResult = ReturnType<typeof useCreateTenantMutation>;
export type CreateTenantMutationResult = Apollo.MutationResult<CreateTenantMutation>;
export type CreateTenantMutationOptions = Apollo.BaseMutationOptions<CreateTenantMutation, CreateTenantMutationVariables>;
// Generated on 2026-05-12T16:33:01+02:00
