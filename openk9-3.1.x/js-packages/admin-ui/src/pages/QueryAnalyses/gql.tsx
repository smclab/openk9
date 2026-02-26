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
  query QueryAnalysis($id: ID!) {
    queryAnalysis(id: $id) {
      id
      name
      description
      stopWords
      annotators {
        edges {
          node {
            id
            name
          }
        }
      }
      rules {
        edges {
          node {
            id
            name
          }
        }
      }
    }
  }
`;

gql`
  query QueryAnalyses($searchText: String, $after: String) {
    queryAnalyses(searchText: $searchText, first: 20, after: $after) {
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
  mutation DeleteQueryAnalysis($id: ID!) {
    deleteQueryAnalysis(queryAnalysisId: $id) {
      id
      name
    }
  }
`;

gql`
  mutation CreateOrUpdateQueryAnalysis(
    $id: ID
    $name: String!
    $description: String
    $stopWords: String
    $annotatorsIds: [BigInteger]
    $rulesIds: [BigInteger]
  ) {
    queryAnalysisWithLists(
      id: $id
      queryAnalysisWithListsDTO: {
        name: $name
        description: $description
        stopWords: $stopWords
        annotatorsIds: $annotatorsIds
        rulesIds: $rulesIds
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

export const QueryAnalysesAssociations = gql`
  query QueryAnalysisAssociations($parentId: ID!, $unassociated: Boolean!) {
    queryAnalysis(id: $parentId) {
      id
      annotators(notEqual: $unassociated) {
        edges {
          node {
            id
            name
          }
        }
      }
      rules(notEqual: $unassociated) {
        edges {
          node {
            id
            name
          }
        }
      }
    }
  }
`;

