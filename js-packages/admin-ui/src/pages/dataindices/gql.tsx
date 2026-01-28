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
    dataIndices(
      searchText: $searchText
      first: $first
      before: $after
      sortByList: [{ column: "modifiedDate", direction: DESC }]
    ) {
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

