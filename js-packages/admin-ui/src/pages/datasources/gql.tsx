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

export const DataSourcesQuery = gql`
  query DataSources($searchText: String, $after: String, $first: Int = 10, $sortByList: [SortByInput!]) {
    datasources(searchText: $searchText, first: $first, after: $after, sortByList: $sortByList) {
      edges {
        node {
          id
          name
          schedulable
          lastIngestionDate
          scheduling
          jsonConfig
          description
          __typename
        }
        __typename
      }
      pageInfo {
        hasNextPage
        endCursor
        __typename
      }
      __typename
    }
  }
`;

gql`
  mutation DeleteDataSource($id: ID!, $datasourceName: String!) {
    deleteDatasource(datasourceId: $id, datasourceName: $datasourceName) {
      id
      name
    }
  }
`;

gql`
  query UnboundBucketsByDatasource($datasourceId: BigInteger!) {
    unboundBucketsByDatasource(datasourceId: $datasourceId) {
      name
      id
    }
  }
`;

export const DataSourceQuery = gql`
  query DataSource($id: ID!, $searchText: String) {
    datasource(id: $id) {
      id
      name
      description
      schedulable
      scheduling
      jsonConfig
      reindexable
      reindexing
      purgeable
      purging
      purgeMaxAge
      lastIngestionDate
      pluginDriver {
        id
        name
        provisioning
        jsonConfig
      }
      dataIndex {
        id
        name
        description
        knnIndex
      }
      enrichPipeline {
        id
        name
      }
      dataIndexes(searchText: $searchText) {
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

export const createDatasource = gql`
  mutation createDatasourceConnection(
    $name: String!
    $description: String
    $schedulable: Boolean!
    $scheduling: String!
    $jsonConfig: String
    $pluginDriverId: BigInteger!
    $pipeline: PipelineWithItemsDTOInput
    $pipelineId: BigInteger
    $reindexable: Boolean!
    $reindexing: String!
    $purgeable: Boolean!
    $purging: String!
    $purgeMaxAge: String!
    $dataIndex: DataIndexDTOInput!
  ) {
    createDatasourceConnection(
      datasourceConnection: {
        name: $name
        description: $description
        schedulable: $schedulable
        scheduling: $scheduling
        jsonConfig: $jsonConfig
        pluginDriverId: $pluginDriverId
        pipeline: $pipeline
        pipelineId: $pipelineId
        reindexable: $reindexable
        reindexing: $reindexing
        purgeable: $purgeable
        purging: $purging
        purgeMaxAge: $purgeMaxAge
        dataIndex: $dataIndex
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

export const updateDataSource = gql`
  mutation updateDatasourceConnection(
    $name: String!
    $description: String
    $schedulable: Boolean!
    $scheduling: String!
    $jsonConfig: String
    $pipeline: PipelineWithItemsDTOInput
    $pipelineId: BigInteger
    $dataIndexId: BigInteger!
    $datasourceId: BigInteger!
    $reindexable: Boolean!
    $reindexing: String!
    $purging: String!
    $purgeable: Boolean!
    $purgeMaxAge: String!
  ) {
    updateDatasourceConnection(
      datasourceConnection: {
        name: $name
        description: $description
        schedulable: $schedulable
        scheduling: $scheduling
        jsonConfig: $jsonConfig
        pipeline: $pipeline
        pipelineId: $pipelineId
        dataIndexId: $dataIndexId
        datasourceId: $datasourceId
        reindexable: $reindexable
        reindexing: $reindexing
        purging: $purging
        purgeable: $purgeable
        purgeMaxAge: $purgeMaxAge
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

export const DatasourceSchedulers = gql`
  query qDatasourceSchedulers($id: ID!, $first: Int, $after: String) {
    datasource(id: $id) {
      id
      schedulers(first: $first, before: $after, sortByList: { column: "modifiedDate", direction: DESC }) {
        edges {
          node {
            id
            status
            modifiedDate
          }
        }
        pageInfo {
          hasNextPage
          endCursor
          __typename
        }
      }
    }
  }
`;

const DataSourceInformation = gql`
  query DataSourceInformation($id: ID!) {
    datasource(id: $id) {
      dataIndex {
        cat {
          docsCount
          docsDeleted
          health
          index
          pri
          priStoreSize
          rep
          status
          storeSize
          uuid
        }
      }
    }
  }
`;

gql`
  query EnrichPipelineOptions($searchText: String, $cursor: String) {
    options: enrichPipelines(searchText: $searchText, after: $cursor) {
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
  query EnrichItems($searchText: String, $after: String) {
    enrichItems(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
          name
          description
          type
          serviceName
          jsonConfig
          script
          behaviorMergeType
          jsonPath
          behaviorOnError
          requestTimeout
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
  query PluginDrivers {
    pluginDriversPageFilter(pageable: {}) {
      content {
        id
        name
        description
        jsonConfig
        provisioning
        type
      }
    }
  }
`;

gql`
  mutation CreateOrUpdatePluginDriver(
    $id: ID
    $name: String!
    $description: String
    $type: PluginDriverType!
    $jsonConfig: String
    $provisioning: Provisioning
  ) {
    pluginDriver(
      id: $id
      pluginDriverDTO: {
        name: $name
        description: $description
        type: $type
        jsonConfig: $jsonConfig
        provisioning: $provisioning
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

