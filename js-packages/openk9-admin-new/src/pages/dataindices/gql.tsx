import { gql } from "@apollo/client";

export const CreateDataIndexMutation = gql`
  mutation CreateDataIndex(
    $name: String!
    $datasourceId: ID!
    $description: String
    $knnIndex: Boolean
    $docTypeIds: [BigInteger]
    $chunkType: ChunkType
    $chunkWindowSize: Int
    $embeddingJsonConfig: String
    $embeddingDocTypeFieldId: BigInteger
    $settings: String
  ) {
    dataIndex(
      datasourceId: $datasourceId
      dataIndexDTO: {
        name: $name
        description: $description
        knnIndex: $knnIndex
        docTypeIds: $docTypeIds
        chunkType: $chunkType
        chunkWindowSize: $chunkWindowSize
        embeddingJsonConfig: $embeddingJsonConfig
        embeddingDocTypeFieldId: $embeddingDocTypeFieldId
        settings: $settings
      }
    ) {
      entity {
        name
      }
    }
  }
`;

export const DataIndicesQuery = gql`
  query DataIndices($searchText: String, $first: Int, $after: String) {
    dataIndices(searchText: $searchText, first: $first, after: $after) {
      edges {
        node {
          id
          name
          description
          createDate
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

export const DataIndexQuery = gql`
  query DataIndex($id: ID!) {
    dataIndex(id: $id) {
      name
      description
      settings
      chunkType
      chunkWindowSize
      embeddingJsonConfig
      knnIndex
      settings
      datasource {
        id
        name
      }
      embeddingDocTypeField {
        id
        name
      }
      docTypes {
        edges {
          node {
            id
            name
          }
        }
      }
      cat {
        docsCount
        docsDeleted
        storeSize
      }
    }
  }
`;

/* Probably not needed*/

gql`
  query DataIndexMapping($id: ID!) {
    dataIndex(id: $id) {
      mappings
    }
  }
`;
