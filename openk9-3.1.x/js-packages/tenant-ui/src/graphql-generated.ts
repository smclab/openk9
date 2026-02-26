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

export type BackgroundProcess = GraphqlId & {
  __typename?: 'BackgroundProcess';
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  id?: Maybe<Scalars['BigInteger']>;
  message?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  name?: Maybe<Scalars['String']>;
  processId?: Maybe<Scalars['String']>;
  status?: Maybe<Status>;
};

/** A connection to a list of items. */
export type Connection_BackgroundProcess = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_BackgroundProcess>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

/** A connection to a list of items. */
export type Connection_Tenant = {
  /** A list of edges. */
  edges?: Maybe<Array<Maybe<Edge_Tenant>>>;
  /** details about this specific page */
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_BackgroundProcess = Connection_BackgroundProcess & {
  __typename?: 'DefaultConnection_BackgroundProcess';
  edges?: Maybe<Array<Maybe<Edge_BackgroundProcess>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultConnection_Tenant = Connection_Tenant & {
  __typename?: 'DefaultConnection_Tenant';
  edges?: Maybe<Array<Maybe<Edge_Tenant>>>;
  pageInfo?: Maybe<PageInfo>;
};

export type DefaultEdge_BackgroundProcess = Edge_BackgroundProcess & {
  __typename?: 'DefaultEdge_BackgroundProcess';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<BackgroundProcess>;
};

export type DefaultEdge_Tenant = Edge_Tenant & {
  __typename?: 'DefaultEdge_Tenant';
  cursor?: Maybe<Scalars['String']>;
  node?: Maybe<Tenant>;
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
export type Edge_BackgroundProcess = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<BackgroundProcess>;
};

/** An edge in a connection */
export type Edge_Tenant = {
  /** cursor marks a unique position or index into the connection */
  cursor?: Maybe<Scalars['String']>;
  /** The item at the end of the edge */
  node?: Maybe<Tenant>;
};

export type FieldValidator = {
  __typename?: 'FieldValidator';
  field?: Maybe<Scalars['String']>;
  message?: Maybe<Scalars['String']>;
};

export type GraphqlId = {
  id?: Maybe<Scalars['BigInteger']>;
};

/** Mutation root */
export type Mutation = {
  __typename?: 'Mutation';
  tenant?: Maybe<Response_Tenant>;
};


/** Mutation root */
export type MutationTenantArgs = {
  id?: InputMaybe<Scalars['ID']>;
  patch?: InputMaybe<Scalars['Boolean']>;
  tenantDTO?: InputMaybe<TenantDtoInput>;
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

/** Query root */
export type Query = {
  __typename?: 'Query';
  backgroundProcess?: Maybe<BackgroundProcess>;
  backgroundProcesses?: Maybe<Connection_BackgroundProcess>;
  tenant?: Maybe<Tenant>;
  tenants?: Maybe<Connection_Tenant>;
};


/** Query root */
export type QueryBackgroundProcessArgs = {
  id: Scalars['ID'];
};


/** Query root */
export type QueryBackgroundProcessesArgs = {
  after?: InputMaybe<Scalars['String']>;
  before?: InputMaybe<Scalars['String']>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  searchText?: InputMaybe<Scalars['String']>;
  sortByList?: InputMaybe<Array<InputMaybe<SortByInput>>>;
};


/** Query root */
export type QueryTenantArgs = {
  id: Scalars['ID'];
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

export type Response_Tenant = {
  __typename?: 'Response_Tenant';
  entity?: Maybe<Tenant>;
  fieldValidators?: Maybe<Array<Maybe<FieldValidator>>>;
};

export type SortByInput = {
  column?: InputMaybe<Scalars['String']>;
  direction?: InputMaybe<Direction>;
};

export enum Status {
  Failed = 'FAILED',
  Finished = 'FINISHED',
  InProgress = 'IN_PROGRESS',
  Roolback = 'ROOLBACK'
}

export type Tenant = GraphqlId & {
  __typename?: 'Tenant';
  clientId?: Maybe<Scalars['String']>;
  clientSecret?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  createDate?: Maybe<Scalars['DateTime']>;
  id?: Maybe<Scalars['BigInteger']>;
  liquibaseSchemaName?: Maybe<Scalars['String']>;
  /** ISO-8601 */
  modifiedDate?: Maybe<Scalars['DateTime']>;
  realmName?: Maybe<Scalars['String']>;
  schemaName?: Maybe<Scalars['String']>;
  virtualHost?: Maybe<Scalars['String']>;
};

export type TenantDtoInput = {
  clientId: Scalars['String'];
  clientSecret?: InputMaybe<Scalars['String']>;
  liquibaseSchemaName: Scalars['String'];
  realmName: Scalars['String'];
  schemaName: Scalars['String'];
  virtualHost: Scalars['String'];
};

export type ProcessesQueryVariables = Exact<{ [key: string]: never; }>;


export type ProcessesQuery = { __typename?: 'Query', backgroundProcesses?: { __typename?: 'DefaultConnection_BackgroundProcess', edges?: Array<{ __typename?: 'DefaultEdge_BackgroundProcess', node?: { __typename?: 'BackgroundProcess', id?: any | null, name?: string | null, createDate?: any | null, status?: Status | null, modifiedDate?: any | null, processId?: string | null } | null } | null> | null, pageInfo?: { __typename?: 'DefaultPageInfo', hasNextPage: boolean, endCursor?: string | null } | null } | null };

export type TenantQueryVariables = Exact<{
  id: Scalars['ID'];
}>;


export type TenantQuery = { __typename?: 'Query', tenant?: { __typename?: 'Tenant', id?: any | null, realmName?: string | null, schemaName?: string | null, modifiedDate?: any | null, virtualHost?: string | null, clientSecret?: string | null, createDate?: any | null } | null };

export type CreateOrUpdateTenantMutationVariables = Exact<{
  id?: InputMaybe<Scalars['ID']>;
  virtualHost: Scalars['String'];
  schemaName: Scalars['String'];
  liquibaseSchemaName: Scalars['String'];
  clientId: Scalars['String'];
  realmName: Scalars['String'];
}>;


export type CreateOrUpdateTenantMutation = { __typename?: 'Mutation', tenant?: { __typename?: 'Response_Tenant', fieldValidators?: Array<{ __typename?: 'FieldValidator', field?: string | null, message?: string | null } | null> | null } | null };

export type TenantsQueryVariables = Exact<{
  searchText?: InputMaybe<Scalars['String']>;
  cursor?: InputMaybe<Scalars['String']>;
}>;


export type TenantsQuery = { __typename?: 'Query', tenants?: { __typename?: 'DefaultConnection_Tenant', edges?: Array<{ __typename?: 'DefaultEdge_Tenant', node?: { __typename?: 'Tenant', id?: any | null, virtualHost?: string | null, createDate?: any | null, modifiedDate?: any | null } | null } | null> | null } | null };


export const ProcessesDocument = gql`
    query Processes {
  backgroundProcesses {
    edges {
      node {
        id
        name
        createDate
        status
        modifiedDate
        processId
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
    `;

/**
 * __useProcessesQuery__
 *
 * To run a query within a React component, call `useProcessesQuery` and pass it any options that fit your needs.
 * When your component renders, `useProcessesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useProcessesQuery({
 *   variables: {
 *   },
 * });
 */
export function useProcessesQuery(baseOptions?: Apollo.QueryHookOptions<ProcessesQuery, ProcessesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<ProcessesQuery, ProcessesQueryVariables>(ProcessesDocument, options);
      }
export function useProcessesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<ProcessesQuery, ProcessesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<ProcessesQuery, ProcessesQueryVariables>(ProcessesDocument, options);
        }
export type ProcessesQueryHookResult = ReturnType<typeof useProcessesQuery>;
export type ProcessesLazyQueryHookResult = ReturnType<typeof useProcessesLazyQuery>;
export type ProcessesQueryResult = Apollo.QueryResult<ProcessesQuery, ProcessesQueryVariables>;
export const TenantDocument = gql`
    query Tenant($id: ID!) {
  tenant(id: $id) {
    id
    realmName
    schemaName
    modifiedDate
    virtualHost
    clientSecret
    createDate
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
export const CreateOrUpdateTenantDocument = gql`
    mutation CreateOrUpdateTenant($id: ID, $virtualHost: String!, $schemaName: String!, $liquibaseSchemaName: String!, $clientId: String!, $realmName: String!) {
  tenant(
    id: $id
    tenantDTO: {virtualHost: $virtualHost, schemaName: $schemaName, liquibaseSchemaName: $liquibaseSchemaName, clientId: $clientId, realmName: $realmName}
  ) {
    fieldValidators {
      field
      message
    }
  }
}
    `;
export type CreateOrUpdateTenantMutationFn = Apollo.MutationFunction<CreateOrUpdateTenantMutation, CreateOrUpdateTenantMutationVariables>;

/**
 * __useCreateOrUpdateTenantMutation__
 *
 * To run a mutation, you first call `useCreateOrUpdateTenantMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateOrUpdateTenantMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createOrUpdateTenantMutation, { data, loading, error }] = useCreateOrUpdateTenantMutation({
 *   variables: {
 *      id: // value for 'id'
 *      virtualHost: // value for 'virtualHost'
 *      schemaName: // value for 'schemaName'
 *      liquibaseSchemaName: // value for 'liquibaseSchemaName'
 *      clientId: // value for 'clientId'
 *      realmName: // value for 'realmName'
 *   },
 * });
 */
export function useCreateOrUpdateTenantMutation(baseOptions?: Apollo.MutationHookOptions<CreateOrUpdateTenantMutation, CreateOrUpdateTenantMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateOrUpdateTenantMutation, CreateOrUpdateTenantMutationVariables>(CreateOrUpdateTenantDocument, options);
      }
export type CreateOrUpdateTenantMutationHookResult = ReturnType<typeof useCreateOrUpdateTenantMutation>;
export type CreateOrUpdateTenantMutationResult = Apollo.MutationResult<CreateOrUpdateTenantMutation>;
export type CreateOrUpdateTenantMutationOptions = Apollo.BaseMutationOptions<CreateOrUpdateTenantMutation, CreateOrUpdateTenantMutationVariables>;
export const TenantsDocument = gql`
    query Tenants($searchText: String, $cursor: String) {
  tenants(searchText: $searchText, first: 25, after: $cursor) {
    edges {
      node {
        id
        virtualHost
        createDate
        modifiedDate
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
// Generated on 2023-01-18T15:33:18+01:00
