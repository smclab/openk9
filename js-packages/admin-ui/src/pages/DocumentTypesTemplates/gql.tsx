import { gql } from "@apollo/client";

 gql`
  query DocumentTypeTemplates($searchText: String, $after: String) {
    docTypeTemplates(searchText: $searchText, first: 20, after: $after) {
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
  mutation DeleteDocumentTypeTemplate($id: ID!) {
    deleteDocTypeTemplate(docTypeTemplateId: $id) {
      id
      name
    }
  }
`;

export const DocumentTypesQuery = gql`
  query DocumentTypes($searchText: String, $cursor: String) {
    docTypes(searchText: $searchText, first: 20, after: $cursor) {
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
  query DocumentTypeTemplate($id: ID!) {
    docTypeTemplate(id: $id) {
      id
      name
      description
      templateType
      source
      compiled
    }
  }
`;

gql`
  mutation CreateOrUpdateDocumentTypeTemplate(
    $id: ID
    $name: String!
    $description: String
    $templateType: TemplateType!
    $source: String!
    $compiled: String!
  ) {
    docTypeTemplate(
      id: $id
      docTypeTemplateDTO: {
        name: $name
        description: $description
        templateType: $templateType
        source: $source
        compiled: $compiled
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
