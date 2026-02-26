/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import { gql } from "@apollo/client";

gql`
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

gql`
  query unassociatedTokenTabsInTab($id: BigInteger!) {
    unboundTabsByTokenTab(tokenTabId: $id) {
      id
      name
    }
  }
`;

