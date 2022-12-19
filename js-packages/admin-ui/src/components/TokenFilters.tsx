import React from "react";
import { gql } from "@apollo/client";
import { useDeleteTokenFiltersMutation, useTokenFiltersQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";

export const TokenFiltersQuery = gql`
  query TokenFilters($searchText: String, $cursor: String) {
    tokenFilters(searchText: $searchText, first: 25, after: $cursor) {
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
  mutation DeleteTokenFilters($id: ID!) {
    deleteTokenFilter(tokenFilterId: $id) {
      id
      name
    }
  }
`;

export function TokenFilters() {
  const showToast = useToast();
  const tokenFiltersQuery = useTokenFiltersQuery();
  const [deleteTokenFiltersMutate] = useDeleteTokenFiltersMutation({
    refetchQueries: [TokenFiltersQuery],
    onCompleted(data) {
      if (data.deleteTokenFilter?.id) {
        showToast({ displayType: "success", title: "Tokenizer deleted", content: data.deleteTokenFilter.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Rule error", content: error.message ?? "" });
    },
  });

  return (
    <Table
      data={{
        queryResult: tokenFiltersQuery,
        field: (data) => data?.tokenFilters,
      }}
      onCreatePath="/token-filters/new"
      onDelete={(tokenfilters) => {
        if (tokenfilters?.id) deleteTokenFiltersMutate({ variables: { id: tokenfilters.id } });
      }}
      columns={[
        { header: "Name", content: (tokenizer) => formatName(tokenizer) },
        { header: "Description", content: (tokenizer) => tokenizer?.description },
      ]}
    />
  );
}
