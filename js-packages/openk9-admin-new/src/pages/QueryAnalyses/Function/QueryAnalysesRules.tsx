import { gql } from "@apollo/client";

gql`
  query QueryAnalysesRules($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    queryAnalysis(id: $parentId) {
      id
      rules(searchText: $searchText, notEqual: $unassociated, first: 20, after: $cursor) {
        edges {
          node {
            id
            name
            lhs
            rhs
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
  mutation AddRulesToQueryAnalyses($childId: ID!, $parentId: ID!) {
    addRuleToQueryAnalysis(ruleId: $childId, id: $parentId) {
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
  mutation RemoveRuleFromQueryAnalyses($childId: ID!, $parentId: ID!) {
    removeRuleFromQueryAnalysis(ruleId: $childId, id: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;
