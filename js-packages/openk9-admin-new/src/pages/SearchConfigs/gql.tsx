import { gql } from "@apollo/client";

export const SearchConfigQuery = gql`
  query SearchConfig($id: ID!) {
    searchConfig(id: $id) {
      id
      name
      description
      minScore
      minScoreSuggestions
      minScoreSearch
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
  ) {
    searchConfig(
      id: $id
      searchConfigDTO: {
        name: $name
        description: $description
        minScore: $minScore
        minScoreSuggestions: $minScoreSuggestions
        minScoreSearch: $minScoreSearch
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

export const SearchConfigsQueryQ = gql`
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
