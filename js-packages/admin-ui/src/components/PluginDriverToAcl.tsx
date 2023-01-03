import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import {
  useAddDocumentTypeFieldToPluginDriversMutation,
  useDocumentTypeFieldsForPluginQuery,
  usePluginDriverToDocumentTypeFieldsQuery,
  useRemoveDocumentTypeFieldFromPluginDriversMutation,
} from "../graphql-generated";
import { AssociatedEntitiesWithSelect } from "./Form";
import React from "react";
gql`
  query PluginDriverToDocumentTypeFields($parentId: ID!, $searchText: String, $cursor: String) {
    pluginDriver(id: $parentId) {
      id
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

gql`
  query DocumentTypeFieldsForPlugin {
    docTypeFields {
      edges {
        node {
          id
          name
          description
        }
      }
    }
  }
`;

gql`
  mutation AddDocumentTypeFieldToPluginDrivers($childId: ID!, $parentId: ID!, $userField: UserField) {
    addDocTypeFieldToPluginDriver(docTypeFieldId: $childId, pluginDriverId: $parentId, userField: $userField) {
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
  mutation RemoveDocumentTypeFieldFromPluginDrivers($childId: ID!, $parentId: ID!) {
    removeDocTypeFieldFromPluginDriver(docTypeFieldId: $childId, pluginDriverId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export function PluginDriverToAcl() {
  const { pluginDriverId } = useParams();
  if (!pluginDriverId) return null;
  return (
    <React.Fragment>
      <AssociatedEntitiesWithSelect
        label="Associate Document Type Fields"
        parentId={pluginDriverId}
        list={{
          useListQuery: usePluginDriverToDocumentTypeFieldsQuery,
          field: (data) => data?.pluginDriver?.docTypeFields,
        }}
        notSelect={useDocumentTypeFieldsForPluginQuery}
        useAddMutation={useAddDocumentTypeFieldToPluginDriversMutation}
        useRemoveMutation={useRemoveDocumentTypeFieldFromPluginDriversMutation}
      />
    </React.Fragment>
  );
}
