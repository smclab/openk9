import { gql } from "@apollo/client";

export const TabTokenQuery = gql`
  query TabTokenTab($id: ID!) {
    tokenTab(id: $id) {
      id
      name
      description
      value
      filter
      tokenType
      docTypeField {
        id
        name
      }
      extraParams
    }
  }
`;

gql`
  mutation CreateOrUpdateTabToken(
    $tokenTabId: ID
    $name: String!
    $description: String
    $value: String!
    $filter: Boolean!
    $tokenType: TokenType!
    $docTypeFieldId: BigInteger
    $extraParams: String
  ) {
    tokenTabWithDocTypeField(
      id: $tokenTabId
      tokenTabWithDocTypeFieldDTO: {
        name: $name
        description: $description
        filter: $filter
        tokenType: $tokenType
        value: $value
        extraParams: $extraParams
        docTypeFieldId: $docTypeFieldId
      }
    ) {
      entity {
        id
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

gql`
  query DocTypeFieldValue($id: ID!) {
    value: docTypeField(id: $id) {
      id
      name
      description
    }
  }
`;

gql`
  query DocTypeFieldOptionsTokenTab($searchText: String, $after: String) {
    options: docTypeFields(searchText: $searchText, first: 20, after: $after) {
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

export const TabTokens = gql`
  query TabTokensQuery($searchText: String, $cursor: String) {
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

gql`
  mutation DeleteTabToken($id: ID!) {
    deleteTokenTab(tokenTabId: $id) {
      id
      name
    }
  }
`;

export const unassociatedTokenTabsInTab = gql`
  query unassociatedTokenTabsInTab($id: BigInteger!) {
    unboundTabsByTokenTab(tokenTabId: $id) {
      id
      name
    }
  }
`;
