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
    }
  }
`;
