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

export const AUTOCOMPLETES = gql`
  query Autocompletes($searchText: String, $after: String) {
    autocompletes(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
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

export const DELETE_AUTOCOMPLETE = gql`
  mutation DeleteAutocomplete($id: ID!) {
    deleteAutocomplete(id: $id) {
      id
      name
    }
  }
`;

export const AUTOCOMPLETE = gql`
  query Autocomplete($id: ID!) {
    autocomplete(id: $id) {
      id
      fuzziness
      minimumShouldMatch
      name
      operator
      resultSize
      fields {
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

export const CREATE_OR_UPDATE_AUTOCOMPLETE = gql`
  mutation CreateOrUpdateAutocomplete($id: ID, $autocompleteDTO: AutocompleteDTOInput!) {
    autocomplete(id: $id, autocompleteDTO: $autocompleteDTO) {
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

gql`
  query UnboundDocTypeFieldByAutocomplete($autocompleteId: BigInteger!) {
    unboundDocTypeFieldByAutocomplete(autocompleteId: $autocompleteId) {
      id
      name
    }
  }
`;

