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
