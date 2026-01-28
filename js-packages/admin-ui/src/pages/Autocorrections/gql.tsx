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

export const autocorrectionsConfigOptions = gql`
  query AutocorrectionsOptions($searchText: String, $cursor: String) {
    autocorrections(searchText: $searchText, first: 5, after: $cursor) {
      edges {
        cursor
        node {
          id
          description
          name
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

export const autocorrectionValue = gql`
  query AutocorrectionValue($id: ID!) {
    autocorrection(id: $id) {
      id
      modifiedDate
      description
      maxEdit
      minWordLength
      name
      prefixLength
      sort
      suggestMode
      enableSearchWithCorrection
      autocorrectionDocTypeField {
        id
        name
      }
    }
  }
`;

gql`
  mutation DeleteAutocorrection($id: ID!) {
    deleteAutocorrection(id: $id) {
      id
    }
  }
`;

gql`
  mutation SaveOrUpdateAutocorrection($id: ID!) {
    deleteAutocorrection(id: $id) {
      id
    }
  }
`;

export const saveOrUpdateAutocorrection = gql`
  mutation saveAutocorrection(
    $id: ID
    $autocorrectionDocTypeFieldId: BigInteger
    $enableSearchWithCorrection: Boolean!
    $maxEdit: Int!
    $minWordLength: Int!
    $prefixLength: Int!
    $sort: SortType!
    $suggestMode: SuggestMode!
    $name: String!
  ) {
    autocorrection(
      id: $id
      autocorrectionDTO: {
        autocorrectionDocTypeFieldId: $autocorrectionDocTypeFieldId
        enableSearchWithCorrection: $enableSearchWithCorrection
        maxEdit: $maxEdit
        minWordLength: $minWordLength
        prefixLength: $prefixLength
        sort: $sort
        suggestMode: $suggestMode
        name: $name
      }
    ) {
      entity {
        id
      }
    }
  }
`;

export const allDocTypeFieldsQuery = gql`
  query AllDocTypeFields {
    docTypeFields {
      edges {
        node {
          id
          name
        }
      }
    }
  }
`;

