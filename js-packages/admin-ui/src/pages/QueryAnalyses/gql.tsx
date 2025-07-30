import { gql } from "@apollo/client";

gql`
  query QueryAnalysis($id: ID!) {
    queryAnalysis(id: $id) {
      id
      name
      description
      stopWords
      annotators {
        edges {
          node {
            id
            name
          }
        }
      }
      rules {
        edges {
          node {
            id
            name
          }
        }
      }
    }
  }
`;

gql`
  query QueryAnalyses($searchText: String, $after: String) {
    queryAnalyses(searchText: $searchText, first: 20, after: $after) {
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
  mutation DeleteQueryAnalysis($id: ID!) {
    deleteQueryAnalysis(queryAnalysisId: $id) {
      id
      name
    }
  }
`;

gql`
  mutation CreateOrUpdateQueryAnalysis(
    $id: ID
    $name: String!
    $description: String
    $stopWords: String
    $annotatorsIds: [BigInteger]
    $rulesIds: [BigInteger]
  ) {
    queryAnalysisWithLists(
      id: $id
      queryAnalysisWithListsDTO: {
        name: $name
        description: $description
        stopWords: $stopWords
        annotatorsIds: $annotatorsIds
        rulesIds: $rulesIds
      }
    ) {
      entity {
        id
        name
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

export const QueryAnalysesAssociations = gql`
  query QueryAnalysisAssociations($parentId: ID!, $unassociated: Boolean!) {
    queryAnalysis(id: $parentId) {
      id
      annotators(notEqual: $unassociated) {
        edges {
          node {
            id
            name
          }
        }
      }
      rules(notEqual: $unassociated) {
        edges {
          node {
            id
            name
          }
        }
      }
    }
  }
`;
