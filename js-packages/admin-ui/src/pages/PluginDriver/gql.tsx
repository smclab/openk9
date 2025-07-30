import { gql } from "@apollo/client";

gql`
  query PluginDriver($id: ID!) {
    pluginDriver(id: $id) {
      id
      name
      description
      type
      jsonConfig
      provisioning
      aclMappings {
        userField
        docTypeField {
          name
          id
          fieldName
        }
      }
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

gql`
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

export const PLUGIN_DRIVER_WITH_DOC_TYPE = gql`
  mutation PluginDriverWithDocType(
    $id: ID
    $name: String!
    $description: String
    $type: PluginDriverType!
    $jsonConfig: String!
    $provisioning: Provisioning!
    $docTypeUserDTOSet: [DocTypeUserDTOInput]
  ) {
    pluginDriverWithDocType(
      id: $id
      pluginWithDocTypeDTO: {
        name: $name
        description: $description
        type: $type
        jsonConfig: $jsonConfig
        provisioning: $provisioning
        docTypeUserDTOSet: $docTypeUserDTOSet
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

export const PLUGIN_DRIVER_RETRIEVER_ASSOCIATED_USER_FIELDS = gql`
  query PluginDriverToDocumentTypeFields($parentId: ID!, $searchText: String, $cursor: String) {
    pluginDriver(id: $parentId) {
      id
      aclMappings {
        userField
        docTypeField {
          id
          name
        }
      }
      docTypeFields(searchText: $searchText, first: 25, after: $cursor) {
        edges {
          node {
            id
            name
            description
            docType {
              id
            }
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

export const PLUGIN_DRIVER_RETRIEVER_USER_FIELDS = gql`
  query DocumentTypeFieldsForPlugin($searchText: String) {
    docTypeFields(searchText: $searchText) {
      edges {
        node {
          id
          name
          description
          __typename
        }
        __typename
      }
      __typename
    }
  }
`;
