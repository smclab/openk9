import { gql } from "@apollo/client";

export const EnrichPipelinesQuery = gql`
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

export const EnrichPipelineQuery = gql`
  query EnrichPipeline($id: ID!) {
    enrichPipeline(id: $id) {
      id
      name
      description
    }
  }
`;

export const AssociatedEnrichPipelineEnrichItemsQuery = gql`
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

export const EnrichPipelineWithItemsQuery = gql`
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
