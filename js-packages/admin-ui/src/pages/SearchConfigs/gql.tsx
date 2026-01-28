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
  query SearchConfig($id: ID!) {
    searchConfig(id: $id) {
      id
      name
      description
      minScore
      minScoreSuggestions
      minScoreSearch
      queryParserConfigs {
        edges {
          node {
            id
            name
            type
            jsonConfig
          }
        }
      }
    }
  }
`;

gql`
  mutation CreateOrUpdateSearchConfig(
    $id: ID
    $name: String!
    $description: String
    $minScore: Float!
    $minScoreSuggestions: Boolean!
    $minScoreSearch: Boolean!
    $queryParsersConfig: [QueryParserConfigDTOInput]
  ) {
    searchConfigWithQueryParsers(
      id: $id
      searchConfigWithQueryParsersDTO: {
        name: $name
        description: $description
        minScore: $minScore
        minScoreSuggestions: $minScoreSuggestions
        minScoreSearch: $minScoreSearch
        queryParsers: $queryParsersConfig
      }
    ) {
      entity {
        id
        name
        minScore
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

gql`
  query SearchConfigs($searchText: String, $first: Int, $after: String) {
    searchConfigs(searchText: $searchText, first: $first, after: $after) {
      edges {
        node {
          id
          name
          description
          minScore
          minScoreSuggestions
          minScoreSearch
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
  mutation DeleteSearchConfig($id: ID!) {
    deleteSearchConfig(searchConfigId: $id) {
      id
      name
    }
  }
`;

export const QueryParserConfig = gql`
  query queryParserConfig {
    queryParserConfigFormConfigurations
  }
`;

