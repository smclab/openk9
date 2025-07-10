import { gql } from "@apollo/client";

export const LargeLanguageModelsQuery = gql`
  query LargeLanguageModels($searchText: String, $after: String) {
    largeLanguageModels(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
          name
          description
          enabled
          providerModel {
            provider
            model
          }
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
  mutation EnableLargeLanguageModel($id: ID!) {
    enableLargeLanguageModel(id: $id) {
      id
      name
    }
  }
`;

gql`
  mutation DeleteLargeLanguageModel($id: ID!) {
    deleteLargeLanguageModel(largeLanguageModelId: $id) {
      id
      name
    }
  }
`;

export const LargeLanguageModelCreate = gql`
  mutation CreateOrUpdateLargeLanguageModel(
    $id: ID
    $apiKey: String
    $apiUrl: String!
    $description: String!
    $name: String!
    $jsonConfig: String
    $providerModel: ProviderModelDTOInput!
    $contextWindow: Int
    $retrieveCitations: Boolean
  ) {
    largeLanguageModel(
      id: $id
      largeLanguageModelDTO: {
        name: $name
        apiKey: $apiKey
        apiUrl: $apiUrl
        description: $description
        jsonConfig: $jsonConfig
        providerModel: $providerModel
        contextWindow: $contextWindow
        retrieveCitations: $retrieveCitations
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

export const LargeLanguageModelQ = gql`
  query LargeLanguageModel($id: ID!) {
    largeLanguageModel(id: $id) {
      name
      description
      apiUrl
      apiKey
      jsonConfig
      contextWindow
      retrieveCitations
      providerModel {
        provider
        model
      }
    }
  }
`;

gql`
  mutation DeleteLargeLanguageModel($id: ID!) {
    deleteLargeLanguageModel(largeLanguageModelId: $id) {
      id
      name
    }
  }
`;
