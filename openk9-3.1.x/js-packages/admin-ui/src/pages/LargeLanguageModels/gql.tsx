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

gql`
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

gql`
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

