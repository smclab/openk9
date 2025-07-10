import { gql } from "@apollo/client";

export const RagConfigurationsQuery = gql`
  query RagConfigurations($searchText: String, $after: String) {
    ragConfigurations(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
          name
          description
          type
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

export const RagConfigurationQuery = gql`
  query RagConfiguration($id: ID!) {
    ragConfiguration(id: $id) {
      id
      name
      description
      type
      reformulate
      chunkWindow
      rephrasePrompt
      prompt
      jsonConfig
      ragToolDescription
      promptNoRag
    }
  }
`;

// export const CreateUpdateRagConfigurationMutation = gql`
//   mutation CreateOrUpdateRagConfiguration(
//     $id: ID
//     $name: String!
//     $description: String
//     $type: RAGType!
//     $reformulate: Boolean
//     $chunkWindow: Int
//     $rephrasePrompt: String
//     $prompt: String
//     $jsonConfig: String
//     $ragToolDescription: String
//     $promptNoRag: String
//   ) {
//     createRAGConfiguration(
//       id: $id
//       ragConfigurationDTO: {
//         name: $name
//         description: $description
//         type: $type
//         reformulate: $reformulate
//         chunkWindow: $chunkWindow
//         rephrasePrompt: $rephrasePrompt
//         prompt: $prompt
//         jsonConfig: $jsonConfig
//         ragToolDescription: $ragToolDescription
//         promptNoRag: $promptNoRag
//       }
//     ) {
//       entity {
//         id
//         name
//         type
//       }
//       fieldValidators {
//         field
//         message
//       }
//     }
//   }
// `;

export const CreateRagConfiguration = gql`
  mutation createRAGConfig(
    $name: String!
    $description: String
    $type: RAGType!
    $reformulate: Boolean
    $chunkWindow: Int
    $rephrasePrompt: String
    $prompt: String
    $jsonConfig: String
    $ragToolDescription: String
    $promptNoRag: String
  ) {
    createRAGConfiguration(
      createRAGConfigurationDTO: {
        name: $name
        description: $description
        type: $type
        reformulate: $reformulate
        chunkWindow: $chunkWindow
        rephrasePrompt: $rephrasePrompt
        prompt: $prompt
        jsonConfig: $jsonConfig
        ragToolDescription: $ragToolDescription
        promptNoRag: $promptNoRag
      }
    ) {
      entity {
        id
        name
        type
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

export const UpdateRagConfiguration = gql`
  mutation updateRAGConfiguration(
    $id: ID!
    $name: String!
    $description: String
    $reformulate: Boolean
    $chunkWindow: Int
    $rephrasePrompt: String
    $prompt: String
    $jsonConfig: String
    $ragToolDescription: String
    $promptNoRag: String
    $patch: Boolean
  ) {
    updateRAGConfiguration(
      id: $id
      patch: $patch
      ragConfigurationDTO: {
        name: $name
        description: $description
        reformulate: $reformulate
        chunkWindow: $chunkWindow
        rephrasePrompt: $rephrasePrompt
        prompt: $prompt
        jsonConfig: $jsonConfig
        ragToolDescription: $ragToolDescription
        promptNoRag: $promptNoRag
      }
    ) {
      entity {
        id
        name
        type
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

export const DeleteRagConfigurationMutation = gql`
  mutation DeleteRagConfiguration($id: ID!) {
    deleteRAGConfiguration(id: $id) {
      id
    }
  }
`;

export const UnboundRagConfigurationsByBucketQuery = gql`
  query UnboundRagConfigurationsByBucket($bucketId: ID!, $ragType: RAGType!) {
    unboundRAGConfigurationByBucket(bucketId: $bucketId, ragType: $ragType) {
      id
      name
    }
  }
`;
