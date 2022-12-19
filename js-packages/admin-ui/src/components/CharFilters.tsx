import React from "react";
import { gql } from "@apollo/client";
import { useCharfiltersQuery, useDeleteCharFiltersMutation } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useToast } from "./ToastProvider";

export const CharFiltersQuery = gql`
  query Charfilters($searchText: String, $cursor: String) {
    charFilters(searchText: $searchText, first: 25, after: $cursor) {
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
  mutation DeleteCharFilters($id: ID!) {
    deleteCharFilter(charFilterId: $id) {
      id
      name
    }
  }
`;

export function CharFilters() {
  const charFiltersQuery = useCharfiltersQuery();
  const showToast = useToast();
  const [deleteCharFilterMutate] = useDeleteCharFiltersMutation({
    refetchQueries: [CharFiltersQuery],
    onCompleted(data) {
      if (data.deleteCharFilter?.id) {
        showToast({ displayType: "success", title: "Char Filter deleted", content: data.deleteCharFilter.name ?? "" });
      }
    },
    onError(error) {
      showToast({ displayType: "danger", title: "Char Filters error", content: error.message ?? "" });
    },
  });

  return (
    <Table
      data={{
        queryResult: charFiltersQuery,
        field: (data) => data?.charFilters,
      }}
      onCreatePath="/char-filters/new"
      onDelete={(charfilter) => {
        if (charfilter?.id) deleteCharFilterMutate({ variables: { id: charfilter.id } });
      }}
      columns={[
        { header: "Name", content: (pluginDriver) => formatName(pluginDriver) },
        { header: "Description", content: (pluginDriver) => pluginDriver?.description },
      ]}
    />
  );
}
