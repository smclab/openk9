import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import { useAddTokenTabToTabMutation, useRemoveTokenTabToTabMutation, useTabTokenTabsQuery } from "../graphql-generated";
import { AssociatedEntities } from "./Form";

gql`
  query TabTokenTabs($parentId: ID!, $searchText: String, $unassociated: Boolean!, $cursor: String) {
    tab(id: $parentId) {
      id
      tokenTabs(searchText: $searchText, notEqual: $unassociated, first: 25, after: $cursor) {
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

gql`
  mutation AddTokenTabToTab($childId: ID!, $parentId: ID!) {
    addTokenTabToTab(id: $parentId, tokenTabId: $childId) {
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
  mutation RemoveTokenTabToTab($childId: ID!, $parentId: ID!) {
    removeTokenTabToTab(id: $parentId, tokenTabId: $childId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

export function TabTokenTabsAssociation() {
  const { tabId } = useParams();
  if (!tabId) return null;
  return (
    <AssociatedEntities
      label="Associate Tab"
      parentId={tabId}
      list={{
        useListQuery: useTabTokenTabsQuery,
        field: (data) => data?.tab?.tokenTabs,
      }}
      useAddMutation={useAddTokenTabToTabMutation}
      useRemoveMutation={useRemoveTokenTabToTabMutation}
    />
  );
}
