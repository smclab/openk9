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
  query Sorting($id: ID!) {
    sorting(id: $id) {
      id
      name
      description
      priority
      defaultSort
      type
      docTypeField {
        id
        name
      }
    }
  }
`;

gql`
  mutation CreateOrUpdateSorting(
    $sortingId: ID
    $name: String!
    $description: String
    $priority: Float!
    $type: SortingType!
    $defaultSort: Boolean!
    $docTypeFieldId: BigInteger
  ) {
    sortingWithDocTypeField(
      id: $sortingId
      sortingWithDocTypeFieldDTO: {
        name: $name
        description: $description
        priority: $priority
        type: $type
        defaultSort: $defaultSort
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

export const Sortings = gql`
  query Sortings($searchText: String, $cursor: String) {
    totalSortings(searchText: $searchText, first: 20, after: $cursor) {
      edges {
        node {
          id
          name
          description
          priority
          defaultSort
          type
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
  mutation DeleteSorting($id: ID!) {
    deleteSorting(sortingId: $id) {
      id
      name
    }
  }
`;
