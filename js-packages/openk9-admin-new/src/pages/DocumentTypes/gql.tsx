import { gql } from "@apollo/client";

export const DocumentTypeTemplatesQuery = gql`
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

export const DocumentTypeTemplateQuery = gql`
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

export const DocumentTypeFieldQuery = gql`
  query DocumentTypeField($id: ID!) {
    docTypeField(id: $id) {
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
      analyzer {
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

gql`
  mutation createOrUpdateDocumentTypeSubFields(
    $parentDocTypeFieldId: ID!
    $name: String!
    $fieldName: String!
    $jsonConfig: String
    $searchable: Boolean!
    $boost: Float
    $fieldType: FieldType!
    $sortable: Boolean!
  ) {
    createSubField(
      parentDocTypeFieldId: $parentDocTypeFieldId
      docTypeFieldDTO: {
        name: $name
        fieldName: $fieldName
        jsonConfig: $jsonConfig
        searchable: $searchable
        boost: $boost
        fieldType: $fieldType
        sortable: $sortable
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
  mutation CreateOrUpdateDocumentType($id: ID, $name: String!, $description: String) {
    docType(id: $id, docTypeDTO: { name: $name, description: $description }) {
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

export const DocumentTypeQuery = gql`
  query DocumentType($id: ID!) {
    docType(id: $id) {
      id
      name
      description
      docTypeTemplate {
        id
        name
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

gql`
  mutation CreateOrUpdateDocumentTypeField(
    $documentTypeId: ID!
    $documentTypeFieldId: ID
    $name: String!
    $fieldName: String!
    $description: String
    $fieldType: FieldType!
    $boost: Float
    $searchable: Boolean!
    $exclude: Boolean
    $jsonConfig: String
    $sortable: Boolean!
    $analyzerId: BigInteger
  ) {
    docTypeFieldWithAnalyzer(
      docTypeId: $documentTypeId
      docTypeFieldId: $documentTypeFieldId
      docTypeFieldWithAnalyzerDTO: {
        name: $name
        description: $description
        fieldType: $fieldType
        boost: $boost
        searchable: $searchable
        exclude: $exclude
        fieldName: $fieldName
        jsonConfig: $jsonConfig
        sortable: $sortable
        analyzerId: $analyzerId
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
  mutation DeleteDocumentType($id: ID!, $docTypeName: String) {
    deleteDocType(docTypeId: $id, docTypeName: $docTypeName) {
      id
    }
  }
`;

gql`
  query docTypeTemplateList {
    docTypeTemplates {
      edges {
        node {
          name
          id
        }
      }
    }
  }
`;

gql`
  mutation CreateOrUpdateDocTypeWithTemplate(
    $name: String!
    $description: String
    $docTypeTemplateId: BigInteger
    $id: ID
  ) {
    docTypeWithTemplate(
      id: $id
      docTypeWithTemplateDTO: { name: $name, description: $description, docTypeTemplateId: $docTypeTemplateId }
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

export const UnboundAnalyzersQuery = gql`
  query UnboundAnalyzers {
    analyzers {
      edges {
        node {
          id
          name
        }
      }
    }
  }
`;
