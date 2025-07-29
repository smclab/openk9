import { gql } from "@apollo/client";

gql`
  query Annotators($searchText: String, $after: String) {
    annotators(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
          name
          description
          size
          type
          fieldName
          fuziness
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
  mutation DeleteAnnotatos($id: ID!) {
    deleteAnnotator(annotatorId: $id) {
      id
      name
    }
  }
`;

gql`
  query DocTypeFieldOptionsAnnotators($searchText: String, $cursor: String) {
    options: docTypeFields(searchText: $searchText, first: 20, after: $cursor) {
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
  query Annotator($id: ID!) {
    annotator(id: $id) {
      id
      fuziness
      size
      type
      description
      name
      fieldName
      docTypeField {
        id
        name
      }
      extraParams
    }
  }
`;

gql`
  mutation CreateOrUpdateAnnotator(
    $id: ID
    $fieldName: String!
    $fuziness: Fuzziness!
    $type: AnnotatorType!
    $description: String
    $size: Int
    $name: String!
    $docTypeFieldId: BigInteger
    $extraParams: String
  ) {
    annotatorWithDocTypeField(
      id: $id
      annotatorDTO: {
        fieldName: $fieldName
        fuziness: $fuziness
        size: $size
        type: $type
        description: $description
        name: $name
        docTypeFieldId: $docTypeFieldId
        extraParams: $extraParams
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

gql`
  query DocTypeFieldOptions($searchText: String, $cursor: String, $annotatorId: ID!) {
    options: docTypeFieldNotInAnnotator(annotatorId: $annotatorId, searchText: $searchText, first: 5, after: $cursor) {
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
  mutation BindDocTypeFieldToDataSource($documentTypeFieldId: ID!, $annotatorId: ID!) {
    bindAnnotatorToDocTypeField(docTypeFieldId: $documentTypeFieldId, id: $annotatorId) {
      left {
        id
        docTypeField {
          id
        }
      }
      right {
        id
      }
    }
  }
`;
gql`
  mutation UnbindDocTypeFieldToDataSource($documentTypeFieldId: ID!, $annotatorId: ID!) {
    unbindAnnotatorFromDocTypeField(docTypeFieldId: $documentTypeFieldId, id: $annotatorId) {
      left {
        id
      }
    }
  }
`;
