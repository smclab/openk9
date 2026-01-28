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

gql`
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

