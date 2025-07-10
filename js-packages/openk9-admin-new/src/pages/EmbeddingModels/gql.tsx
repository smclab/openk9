import { gql } from "@apollo/client";

export const EmbeddingModelsQuery = gql`
  query EmbeddingModels($searchText: String, $after: String) {
    embeddingModels(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
          name
          description
          enabled
        }
      }
    }
  }
`;

gql`
  mutation EnableEmbeddingModel($id: ID!) {
    enableEmbeddingModel(id: $id) {
      id
      name
    }
  }
`;

gql`
  mutation DeleteEmbeddingModel($id: ID!) {
    deleteEmbeddingModel(embeddingModelId: $id) {
      id
      name
    }
  }
`;

export const EmbeddingModelQuery = gql`
  query EmbeddingModel($id: ID!) {
    embeddingModel(id: $id) {
      name
      description
      apiUrl
      apiKey
      vectorSize
      jsonConfig
      providerModel {
        provider
        model
      }
    }
  }
`;

gql`
  mutation CreateOrUpdateEmbeddingModel(
    $id: ID
    $apiKey: String
    $apiUrl: String!
    $description: String!
    $name: String!
    $vectorSize: Int!
    $providerModel: ProviderModelDTOInput!
    $jsonConfig: String
  ) {
    embeddingModel(
      id: $id
      embeddingModelDTO: {
        name: $name
        apiKey: $apiKey
        apiUrl: $apiUrl
        description: $description
        vectorSize: $vectorSize
        providerModel: $providerModel
        jsonConfig: $jsonConfig
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
