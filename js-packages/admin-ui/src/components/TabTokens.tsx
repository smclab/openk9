import React from "react";
import { gql } from "@apollo/client";
import { useCreateOrUpdateTabTokenMutation, useDeleteTabTokenTabMutation, useTabTokensQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useParams } from "react-router-dom";
import { ClayToggle } from "@clayui/form";
import { StyleToggle } from "./Form";

export const TabTokens = gql`
  query TabTokens($tabId: ID!, $searchText: String, $cursor: String) {
    tokenTabs(tabId: $tabId, searchText: $searchText, first: 25, after: $cursor) {
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
  mutation DeleteTabTokenTab($tabId: ID!, $TabTokenTabs: ID!) {
    removeTokenTab(tabId: $tabId, tokenTabId: $TabTokenTabs) {
      right
    }
  }
`;

export function TabTokenTabs() {
  const { tabId } = useParams();
  const tabTokensQuery = useTabTokensQuery({
    variables: { tabId: tabId! },
    skip: !tabId,
  });
  const [deleteTabTokenMutate] = useDeleteTabTokenTabMutation({
    refetchQueries: [TabTokens],
  });
  const [updateTabTokenMutate] = useCreateOrUpdateTabTokenMutation({
    refetchQueries: [TabTokens],
  });
  if (!tabId) throw new Error();
  return (
    <Table
      data={{
        queryResult: tabTokensQuery,
        field: (data) => data?.tokenTabs,
      }}
      label="Tab Tokens"
      onCreatePath="new"
      onDelete={(tabToken) => {
        if (tabToken?.id) deleteTabTokenMutate({ variables: { tabId: tabId, TabTokenTabs: tabToken.id } });
      }}
      columns={[
        { header: "Name", content: (tabToken) => formatName(tabToken) },
        { header: "Token Type", content: (tabToken) => tabToken?.tokenType },
        { header: "Value", content: (tabToken) => tabToken?.value },
        {
          header: "Filter",
          content: (tabToken) => (
            <React.Fragment>
              {" "}
              <ClayToggle
                toggled={tabToken?.filter ?? false}
                onToggle={(filter) => {
                  if (tabToken && tabToken.id && tabToken.name && tabToken.tokenType && tabToken.value) {
                    updateTabTokenMutate({
                      variables: {
                        tabId,
                        tabTokenId: tabToken.id,
                        filter,
                        name: tabToken.name,
                        tokenType: tabToken.tokenType,
                        value: tabToken.value,
                      },
                    });
                  }
                }}
              />{" "}
              <style type="text/css">{StyleToggle}</style>
            </React.Fragment>
          ),
        },
      ]}
    />
  );
}
