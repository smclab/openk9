import { gql } from "@apollo/client";

gql`
  query SearchConfig($id: ID!) {
    searchConfig(id: $id) {
      id
      name
      description
      minScore
      minScoreSuggestions
      minScoreSearch
      queryParserConfigs {
        edges {
          node {
            id
            name
            type
            jsonConfig
          }
        }
      }
    }
  }
`;

gql`
  mutation CreateOrUpdateSearchConfig(
    $id: ID
    $name: String!
    $description: String
    $minScore: Float!
    $minScoreSuggestions: Boolean!
    $minScoreSearch: Boolean!
    $queryParsersConfig: [QueryParserConfigDTOInput]
  ) {
    searchConfigWithQueryParsers(
      id: $id
      searchConfigWithQueryParsersDTO: {
        name: $name
        description: $description
        minScore: $minScore
        minScoreSuggestions: $minScoreSuggestions
        minScoreSearch: $minScoreSearch
        queryParsers: $queryParsersConfig
      }
    ) {
      entity {
        id
        name
        minScore
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

gql`
  query SearchConfigs($searchText: String, $after: String) {
    searchConfigs(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
          name
          description
          minScore
          minScoreSuggestions
          minScoreSearch
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
  mutation DeleteSearchConfig($id: ID!) {
    deleteSearchConfig(searchConfigId: $id) {
      id
      name
    }
  }
`;

export const QueryParserConfig = gql`
  query queryParserConfig {
    queryParserConfigFormConfigurations
  }
`;
