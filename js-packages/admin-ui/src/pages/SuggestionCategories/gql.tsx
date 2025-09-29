import { gql } from "@apollo/client";

gql`
  query UnboundBucketsBySuggestionCategory($id: BigInteger!) {
    unboundBucketsBySuggestionCategory(suggestionCategoryId: $id) {
      name
      id
    }
  }
`;

gql`
  query SuggestionCategories($searchText: String, $after: String) {
    suggestionCategories(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
          name
          description
          priority
          multiSelect
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
  mutation DeleteSuggestionCategory($id: ID!) {
    deleteSuggestionCategory(suggestionCategoryId: $id) {
      id
      name
    }
  }
`;

gql`
  query SuggestionCategoryDocumentTypeFields(
    $parentId: ID!
    $searchText: String
    $unassociated: Boolean!
    $cursor: String
  ) {
    suggestionCategory(id: $parentId) {
      id
      docTypeField {
        name
        subFields(searchText: $searchText, notEqual: $unassociated, first: 20, after: $cursor) {
          edges {
            node {
              id
              name
              description
              docType {
                id
              }
            }
          }
          pageInfo {
            hasNextPage
            endCursor
          }
        }
      }
    }
  }
`;

gql`
  mutation AddDocumentTypeFieldToSuggestionCategory($childId: ID!, $parentId: ID!) {
    addDocTypeFieldToSuggestionCategory(docTypeFieldId: $childId, suggestionCategoryId: $parentId) {
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
  query SuggestionCategory($id: ID!) {
    suggestionCategory(id: $id) {
      id
      name
      description
      priority
      multiSelect
      docTypeField {
        id
        name
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

export const ADD_SUGGESTION_CATEGORY_TRANSLATION = gql`
  mutation AddSuggestionCategoryTranslation(
    $suggestionCategoryId: ID!
    $language: String!
    $key: String
    $value: String!
  ) {
    addSuggestionCategoryTranslation(
      suggestionCategoryId: $suggestionCategoryId
      language: $language
      key: $key
      value: $value
    ) {
      left
      right
    }
  }
`;

gql`
  query UnboundDocTypeFieldsBySuggestionCategory($suggestionCategoryId: BigInteger!) {
    unboundDocTypeFieldsBySuggestionCategory(suggestionCategoryId: $suggestionCategoryId) {
      id
      name
    }
  }
`;

export const DocTypeFields = gql`
  query DocTypeFields($after: String) {
    docTypeFields(first: 20, after: $after) {
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

gql`
  mutation CreateOrUpdateSuggestionCategory(
    $id: ID
    $name: String!
    $description: String
    $priority: Float!
    $multiSelect: Boolean!
    $docTypeFieldId: BigInteger
  ) {
    suggestionCategoryWithDocTypeField(
      id: $id
      suggestionCategoryWithDocTypeFieldDTO: {
        name: $name
        description: $description
        priority: $priority
        multiSelect: $multiSelect
        docTypeFieldId: $docTypeFieldId
      }
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

export const DocumentTypeFieldsParentQuery = gql`
  query DocTypeFieldsByParent($searchText: String, $parentId: BigInteger!, $docTypeId: ID!) {
    docTypeFieldsFromDocTypeByParent(parentId: $parentId, searchText: $searchText, first: 30, docTypeId: $docTypeId) {
      edges {
        node {
          id
          name
          description
          fieldType
          boost
          searchable
          exclude
          fieldName
          jsonConfig
          sortable
          parent {
            id
            fieldName
          }
        }
      }
    }
  }
`;
