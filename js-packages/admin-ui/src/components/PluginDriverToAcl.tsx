import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import {
  useAddDocumentTypeFieldToPluginDriversMutation,
  useChangeUserfieldMutation,
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

const documentTypeFieldsFortPlugin = gql`
  query DocumentTypeFieldsForPlugin($searchText: String) {
    docTypeFields(searchText: $searchText) {
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

gql`
  mutation ChangeUserfield($docTypeFieldId: ID!, $pluginDriverId: ID!, $userField: UserField) {
    userField(docTypeFieldId: $docTypeFieldId, pluginDriverId: $pluginDriverId, userField: $userField) {
      userField
    }
  }
`;

export function PluginDriverToAcl() {
  const { pluginDriverId } = useParams();
  const [ChangeUserfield] = useChangeUserfieldMutation({
    refetchQueries: [{ query: documentTypeFieldsFortPlugin }],
  });
  if (!pluginDriverId) return null;

  return (
    <React.Fragment>
      <AssociatedEntitiesWithSelect
        label="Associate Plugin Driver To Acl"
        parentId={pluginDriverId}
        list={{
          useListQuery: usePluginDriverToDocumentTypeFieldsQuery,
          field: (data) => data?.pluginDriver?.docTypeFields,
        }}
        remove={(id: string, userfield: any) => {
          if (id && userfield) {
            ChangeUserfield({ variables: { pluginDriverId: pluginDriverId, docTypeFieldId: id, userField: userfield } });
          }
        }}
        notSelect={useDocumentTypeFieldsForPluginQuery}
        useAddMutation={useAddDocumentTypeFieldToPluginDriversMutation}
        useRemoveMutation={useRemoveDocumentTypeFieldFromPluginDriversMutation}
      />
    </React.Fragment>
  );
}

export const aclOption = ["EMAIL", "NAME", "NAMESURNAME", "ROLES", "SURNAME", "USERNAME"];
