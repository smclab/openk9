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
  query EnrichPipelines($searchText: String, $after: String) {
    enrichPipelines(searchText: $searchText, first: 20, after: $after) {
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

export const EnrichPipelinesOptionsQuery = gql`
  query EnrichPipelinesValueOptions($id: BigInteger!) {
    unboundEnrichPipelines(itemId: $id) {
      name
      id
    }
  }
`;

export const DeletePipelines = gql`
  mutation DeleteEnrichPipeline($id: ID!) {
    deleteEnrichPipeline(enrichPipelineId: $id) {
      id
      name
    }
  }
`;

gql`
  query EnrichPipeline($id: ID!) {
    enrichPipeline(id: $id) {
      id
      name
      description
    }
  }
`;

gql`
  query AssociatedEnrichPipelineEnrichItems($enrichPipelineId: ID!) {
    enrichPipeline(id: $enrichPipelineId) {
      id
      enrichItems {
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
  }
`;

export const UnassociatedEnrichPipelineEnrichItemsQuery = gql`
  query UnassociatedEnrichPipelineEnrichItems($enrichPipelineId: ID!, $searchText: String) {
    enrichPipeline(id: $enrichPipelineId) {
      id
      enrichItems(searchText: $searchText, not: true, first: 20) {
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
  }
`;

/* da controllare */

gql`
  mutation CreateOrUpdateEnrichPipeline($id: ID, $name: String!, $description: String) {
    enrichPipeline(id: $id, enrichPipelineDTO: { name: $name, description: $description }) {
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
  mutation AddEnrichItemToEnrichPipeline($childId: ID!, $parentId: ID!) {
    addEnrichItemToEnrichPipeline(enrichItemId: $childId, enrichPipelineId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

gql`
  mutation RemoveEnrichItemFromEnrichPipeline($childId: ID!, $parentId: ID!) {
    removeEnrichItemFromEnrichPipeline(enrichItemId: $childId, enrichPipelineId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

gql`
  mutation SortEnrichItems($enrichPipelineId: ID!, $enrichItemIdList: [BigInteger]) {
    sortEnrichItems(enrichPipelineId: $enrichPipelineId, enrichItemIdList: $enrichItemIdList) {
      id
      enrichItems {
        edges {
          node {
            id
            name
            description
          }
        }
      }
    }
  }
`;

gql`
  mutation EnrichPipelineWithItems($id: ID, $items: [ItemDTOInput], $name: String!, $description: String!) {
    enrichPipelineWithEnrichItems(
      id: $id
      pipelineWithItemsDTO: { items: $items, name: $name, description: $description }
    ) {
      entity {
        name
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

