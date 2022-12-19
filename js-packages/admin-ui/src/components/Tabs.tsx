import React from "react";
import { gql } from "@apollo/client";
import { useDeleteTabsMutation, useTabsQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";

export const TabsQuery = gql`
  query Tabs($searchText: String, $cursor: String) {
    tabs(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
          priority
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
  mutation DeleteTabs($id: ID!) {
    deleteTab(tabId: $id) {
      id
      name
    }
  }
`;

export function Tabs() {
  const tabsQuery = useTabsQuery();
  const showToast = useToast();
  const [deleteTabMutate] = useDeleteTabsMutation({
    refetchQueries: [TabsQuery],
    onCompleted(data) {
      if (data.deleteTab?.id) {
        showToast({ displayType: "success", title: "Tab deleted", content: data.deleteTab.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Rule error", content: error.message ?? "" });
    },
  });

  return (
    <Table
      data={{
        queryResult: tabsQuery,
        field: (data) => data?.tabs,
      }}
      onCreatePath="/tabs/new"
      onDelete={(tab) => {
        if (tab?.id) deleteTabMutate({ variables: { id: tab.id } });
      }}
      columns={[
        { header: "Name", content: (tab) => formatName(tab) },
        { header: "Description", content: (tab) => tab?.description },
        { header: "Priority", content: (tab) => tab?.priority },
      ]}
    />
  );
}
