import { gql } from "@apollo/client";

gql`
  query Rules($searchText: String, $after: String) {
    rules(searchText: $searchText, first: 20, after: $after) {
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
`;

gql`
  mutation DeleteRules($id: ID!) {
    deleteRule(ruleId: $id) {
      id
      name
    }
  }
`;

gql`
  query Rule($id: ID!) {
    rule: rule(id: $id) {
      id
      name
      description
      lhs
      rhs
    }
  }
`;

gql`
  mutation CreateOrUpdateRuleQuery($id: ID, $name: String!, $description: String, $lhs: String!, $rhs: String!) {
    rule(id: $id, ruleDTO: { name: $name, description: $description, lhs: $lhs, rhs: $rhs }) {
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
