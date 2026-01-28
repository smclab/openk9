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

gql`
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

