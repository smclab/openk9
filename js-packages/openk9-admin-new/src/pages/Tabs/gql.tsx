import { gql } from "@apollo/client";
import { AssociatedUnassociated } from "utils";

gql`
  query Tabs($searchText: String, $after: String) {
    tabs(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
          name
          description
          priority
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
  mutation DeleteTabs($id: ID!) {
    deleteTab(tabId: $id) {
      id
      name
    }
  }
`;

gql`
  query UnboundBucketsByTab($id: BigInteger!) {
    unboundBucketsByTab(tabId: $id) {
      name
      id
    }
  }
`;

gql`
  query Tab($id: ID!, $unasociated: Boolean) {
    tab(id: $id) {
      id
      name
      description
      priority
      tokenTabs(notEqual: $unasociated) {
        edges {
          node {
            name
            id
          }
        }
      }
      translations {
        key
        language
        value
        description
      }
    }
  }
`;

gql`
  mutation CreateOrUpdateTab(
    $id: ID
    $name: String!
    $description: String
    $priority: Int!
    $tokenTabIds: [BigInteger]
  ) {
    tabWithTokenTabs(
      id: $id
      tabWithTokenTabsDTO: { name: $name, description: $description, priority: $priority, tokenTabIds: $tokenTabIds }
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

export const TabTokensQuery = gql`
  query TabTokenTabs($parentId: ID!, $searchText: String, $cursor: String, $unassociated: Boolean!) {
    tab(id: $parentId) {
      id
      tokenTabs(searchText: $searchText, notEqual: $unassociated, first: 20, after: $cursor) {
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
  mutation AddTokenTabToTab($childId: ID!, $parentId: ID!) {
    addTokenTabToTab(id: $parentId, tokenTabId: $childId) {
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
  mutation RemoveTokenTabToTab($childId: ID!, $parentId: ID!) {
    removeTokenTabToTab(id: $parentId, tokenTabId: $childId) {
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
  query TabTokens($searchText: String, $cursor: String) {
    totalTokenTabs(searchText: $searchText, first: 20, after: $cursor) {
      edges {
        node {
          id
          name
          tokenType
          value
          filter
          extraParams
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

export type ReturnUserTabData = {
  tokenTab: AssociatedUnassociated;
};
