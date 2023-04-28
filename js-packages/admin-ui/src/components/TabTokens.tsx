import React from "react";
import { gql } from "@apollo/client";
import { useCreateOrUpdateTabTokenMutation, useDeleteTabTokenMutation, useTabTokensQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useParams } from "react-router-dom";
import { ClayToggle } from "@clayui/form";
import { StyleToggle } from "./Form";
import { useToast } from "./ToastProvider";

export const TabTokens = gql`
  query TabTokens($searchText: String, $cursor: String) {
    totalTokenTabs(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          tokenType
          value
          filter
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
  mutation DeleteTabToken($id: ID!) {
    deleteTokenTab(tokenTabId: $id) {
      id
      name
    }
  }
`;

export function TabTokenTabs() {
  const showToast = useToast();
  const tabTokensQuery = useTabTokensQuery();

  const [deleteTokenTabMutate] = useDeleteTabTokenMutation({
    refetchQueries: [TabTokens],
    onCompleted(data) {
      if (data.deleteTokenTab?.id) {
        showToast({ displayType: "success", title: "Token Tab deleted", content: data.deleteTokenTab.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Rule error", content: error.message ?? "" });
    },
  });
  return (
    <Table
      data={{
        queryResult: tabTokensQuery,
        field: (data) => data?.totalTokenTabs,
      }}
      label="Tab Tokens"
      onCreatePath="new"
      onDelete={(tabToken) => {
        if (tabToken?.id) deleteTokenTabMutate({ variables: { id: tabToken.id } });
      }}
      columns={[
        { header: "Name", content: (tabToken) => formatName(tabToken) },
        { header: "Token Type", content: (tabToken) => tabToken?.tokenType },
        { header: "Value", content: (tabToken) => tabToken?.value },
      ]}
    />
  );
}
