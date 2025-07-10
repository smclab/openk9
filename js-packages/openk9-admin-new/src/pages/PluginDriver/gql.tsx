import { gql } from "@apollo/client";

export const PluginDriverQuery = gql`
  query PluginDriver($id: ID!) {
    pluginDriver(id: $id) {
      id
      name
      description
      type
      jsonConfig
      provisioning
    }
  }
`;

export type ConfigType = {
  baseUri: string;
  secure: boolean;
  path: string;
  method: string;
};

gql`
  mutation CreateOrUpdatePluginDriverMutation(
    $id: ID
    $name: String!
    $description: String
    $type: PluginDriverType!
    $jsonConfig: String
    $provisioning: Provisioning!
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

const PluginDriverByNameQuery = gql`
  query PluginDriverByName($name: String) {
    pluginDrivers(searchText: $name, first: 1) {
      edges {
        node {
          id
        }
      }
    }
  }
`;

export const PluginDriversInfoQuery = gql`
  query PluginDriversInfoQuery($searchText: String, $after: String) {
    pluginDrivers(searchText: $searchText, first: 20, after: $after) {
      edges {
        node {
          id
          name
          description
          type
          aclMappings {
            userField
            docTypeField {
              fieldName
            }
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
  mutation DeletePluginDriver($id: ID!) {
    deletePluginDriver(pluginDriverId: $id) {
      id
      name
    }
  }
`;
