import { gql } from "@apollo/client";

gql`
  query Buckets($searchText: String, $after: String) {
    buckets(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
          name
          description
          enabled
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

export const queryAnalysisConfigOptions = gql`
  query QueryAnalysisOptions($searchText: String, $cursor: String) {
    options: queryAnalyses(searchText: $searchText, first: 5, after: $cursor) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;
gql`
  query QueryAnalysisValue($id: ID!) {
    value: queryAnalysis(id: $id) {
      id
      name
      description
    }
  }
`;
gql`
  mutation BindQueryAnalysisToBucket($bucketId: ID!, $queryAnalysis: ID!) {
    bindQueryAnalysisToBucket(bucketId: $bucketId, queryAnalysisId: $queryAnalysis) {
      left {
        id
        queryAnalysis {
          id
        }
      }
      right {
        id
      }
    }
  }
`;
gql`
  mutation UnbindQueryAnalysisFromBucket($bucketId: ID!) {
    unbindQueryAnalysisFromBucket(bucketId: $bucketId) {
      right {
        id
      }
    }
  }
`;

export const searchConfigOptions = gql`
  query SearchConfigOptions($searchText: String, $first: Int, $cursor: String) {
    options: searchConfigs(searchText: $searchText, first: $first, after: $cursor) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;
gql`
  query SearchConfigValue($id: ID!) {
    value: searchConfig(id: $id) {
      id
      name
      description
    }
  }
`;
gql`
  mutation BindSearchConfigToBucket($bucketId: ID!, $searchConfigId: ID!) {
    bindSearchConfigToBucket(bucketId: $bucketId, searchConfigId: $searchConfigId) {
      left {
        id
        searchConfig {
          id
        }
      }
      right {
        id
      }
    }
  }
`;
gql`
  mutation UnbindSearchConfigFromBucket($bucketId: ID!) {
    unbindSearchConfigFromBucket(bucketId: $bucketId) {
      right {
        id
      }
    }
  }
`;

gql`
  query LanguagesOptions($searchText: String, $cursor: String) {
    options: languages(searchText: $searchText, after: $cursor) {
      edges {
        node {
          id
          name
          value
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;
gql`
  query LanguageValue($id: ID!) {
    value: language(id: $id) {
      id
      name
      value
    }
  }
`;
gql`
  mutation BindLanguageToBucket($bucketId: ID!, $languageId: ID!) {
    bindLanguageToBucket(bucketId: $bucketId, languageId: $languageId) {
      left {
        id
        language {
          id
        }
      }
      right {
        id
      }
    }
  }
`;
gql`
  mutation UnbindLanguageFromBucket($bucketId: ID!) {
    unbindLanguageFromBucket(bucketId: $bucketId) {
      right {
        id
      }
    }
  }
`;

export const BucketQueryBucket = gql`
  query Bucket($id: ID!) {
    bucket(id: $id) {
      id
      name
      description
      enabled
      refreshOnDate
      refreshOnQuery
      refreshOnSuggestionCategory
      refreshOnTab
      retrieveType
      queryAnalysis {
        id
        name
      }
      searchConfig {
        id
        name
      }
      ragConfigurationChat {
        id
        name
      }
      ragConfigurationChatTool {
        id
        name
      }
      ragConfigurationSimpleGenerate {
        id
        name
      }
      language {
        id
        name
      }
      autocorrection {
        id
        name
      }
    }
  }
`;

gql`
  mutation EnableBucket($id: ID!) {
    enableBucket(id: $id) {
      id
      name
    }
  }
`;

gql`
  mutation DeleteBucket($id: ID!) {
    deleteBucket(bucketId: $id) {
      id
      name
    }
  }
`;

gql`
  query BucketDataSources($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    bucket(id: $parentId) {
      id
      tabs(searchText: $searchText, first: 20, after: $cursor, notEqual: $unassociated) {
        edges {
          node {
            name
            id
          }
        }
      }
      suggestionCategories(searchText: $searchText, first: 20, after: $cursor, notEqual: $unassociated) {
        edges {
          node {
            id
            name
          }
        }
      }
      datasources(searchText: $searchText, first: 20, after: $cursor, notEqual: $unassociated) {
        edges {
          node {
            id
            name
          }
        }

        pageInfo {
          hasNextPage
          endCursor
        }
      }
    }
  }
`;

export const CreateUpdateBucketRecap = gql`
  mutation CreateOrUpdateBucket(
    $id: ID
    $name: String!
    $description: String
    $refreshOnDate: Boolean!
    $refreshOnQuery: Boolean!
    $refreshOnSuggestionCategory: Boolean!
    $refreshOnTab: Boolean!
    $retrieveType: RetrieveType!
    $datasourceIds: [BigInteger]
    $suggestionCategoryIds: [BigInteger]
    $tabIds: [BigInteger]
    $queryAnalysisId: BigInteger
    $defaultLanguageId: BigInteger
    $searchConfigId: BigInteger
    $ragConfigurationChat: BigInteger
    $ragConfigurationChatTool: BigInteger
    $ragConfigurationSimpleGenerate: BigInteger
    $autocorrection: BigInteger
  ) {
    bucketWithLists(
      id: $id
      bucketWithListsDTO: {
        name: $name
        description: $description
        refreshOnDate: $refreshOnDate
        refreshOnQuery: $refreshOnQuery
        refreshOnSuggestionCategory: $refreshOnSuggestionCategory
        refreshOnTab: $refreshOnTab
        retrieveType: $retrieveType
        datasourceIds: $datasourceIds
        suggestionCategoryIds: $suggestionCategoryIds
        tabIds: $tabIds
        queryAnalysisId: $queryAnalysisId
        defaultLanguageId: $defaultLanguageId
        searchConfigId: $searchConfigId
        ragConfigurationChat: $ragConfigurationChat
        ragConfigurationChatTool: $ragConfigurationChatTool
        ragConfigurationSimpleGenerate: $ragConfigurationSimpleGenerate
        autocorrection: $autocorrection
      }
    ) {
      entity {
        id
        name
        enabled
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

/* Probably not needed */
export const AddDataSourceToBucket = gql`
  mutation AddDataSourceToBucket($childId: ID!, $parentId: ID!) {
    addDatasourceToBucket(datasourceId: $childId, bucketId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

/* da controllare */

export const RemoveDataSourceFromBucket = gql`
  mutation RemoveDataSourceFromBucket($childId: ID!, $parentId: ID!) {
    removeDatasourceFromBucket(datasourceId: $childId, bucketId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export const Bucketslanguages = gql`
  query BucketLanguages($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    bucket(id: $parentId) {
      id
      languages(searchText: $searchText, first: 20, after: $cursor, notEqual: $unassociated) {
        edges {
          node {
            id
            name
            value
          }
        }
        pageInfo {
          hasNextPage
          endCursor
        }
      }
    }
  }
`;

export const AddLanguageToBucket = gql`
  mutation AddLanguageToBucket($childId: ID!, $parentId: ID!) {
    addLanguageToBucket(languageId: $childId, bucketId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export const RemoveLanguageFromBucket = gql`
  mutation RemoveLanguageFromBucket($childId: ID!, $parentId: ID!) {
    removeLanguageFromBucket(languageId: $childId, bucketId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export const BucketsTabs = gql`
  query BucketTabs($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    bucket(id: $parentId) {
      id
      tabs(searchText: $searchText, notEqual: $unassociated, first: 20, after: $cursor) {
        edges {
          node {
            id
            name
            description
          }
        }
        pageInfo {
          hasNextPage
          endCursor
        }
      }
    }
  }
`;

gql`
  mutation AddTabToBucket($childId: ID!, $parentId: ID!) {
    addTabToBucket(tabId: $childId, id: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

gql`
  mutation RemoveTabFromBucket($childId: ID!, $parentId: ID!) {
    removeTabFromBucket(tabId: $childId, id: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

gql`
  mutation AddAutocorrectionToBucket($autocorrectionId: ID!, $parentId: ID!) {
    bindAutocorrectionToBucket(autocorrectionId: $autocorrectionId, bucketId: $parentId) {
      left {
        id
      }
    }
  }
`;

gql`
  mutation RemoveAutocorrectionFromBucket($parentId: ID!) {
    unbindAutocorrectionFromBucket(bucketId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export const BucketsSuggestionCategories = gql`
  query BucketSuggestionCategories($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    bucket(id: $parentId) {
      id
      suggestionCategories(searchText: $searchText, notEqual: $unassociated, first: 20, after: $cursor) {
        edges {
          node {
            id
            name
            description
          }
        }
        pageInfo {
          hasNextPage
          endCursor
        }
      }
    }
  }
`;
export const BucketsAutocorrection = gql`
  query unboundAutocorrectionByBucket($parentId: ID!) {
    bucket(id: $parentId) {
      id
      name
    }
  }
`;

gql`
  mutation AddSuggestionCategoryToBucket($childId: ID!, $parentId: ID!) {
    addSuggestionCategoryToBucket(suggestionCategoryId: $childId, bucketId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

gql`
  mutation RemoveSuggestionCategoryFromBucket($childId: ID!, $parentId: ID!) {
    removeSuggestionCategoryFromBucket(suggestionCategoryId: $childId, bucketId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;
